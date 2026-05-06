package mekanism.common.attachments.containers.item;

import java.util.function.BiPredicate;
import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.SerializationConstants;
import mekanism.api.SerializerHelper;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.attachments.containers.ComponentBackedContainer;
import mekanism.common.attachments.containers.ContainerType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.NotNull;

@NothingNullByDefault//TODO - 26.1: Do we want this to implement ItemStackResourceHandler?
public class ComponentBackedInventorySlot extends ComponentBackedContainer<ItemStack, AttachedItems> implements IInventorySlot {

    private final BiPredicate<ItemResource, AutomationType> canExtract;
    private final BiPredicate<ItemResource, AutomationType> canInsert;
    private final Predicate<ItemResource> validator;
    private final boolean obeyStackLimit;
    private final int limit;

    public ComponentBackedInventorySlot(ItemStack attachedTo, int slotIndex, BiPredicate<ItemResource, AutomationType> canExtract,
          BiPredicate<ItemResource, AutomationType> canInsert, Predicate<@NotNull ItemResource> validator) {
        this(attachedTo, slotIndex, canExtract, canInsert, validator, true, Item.ABSOLUTE_MAX_STACK_SIZE);
    }

    public ComponentBackedInventorySlot(ItemStack attachedTo, int slotIndex, BiPredicate<ItemResource, AutomationType> canExtract,
          BiPredicate<ItemResource, AutomationType> canInsert, Predicate<@NotNull ItemResource> validator, boolean obeyStackLimit, int limit) {
        super(attachedTo, slotIndex);
        this.canExtract = canExtract;
        this.canInsert = canInsert;
        this.validator = validator;
        this.obeyStackLimit = obeyStackLimit;
        this.limit = limit;
    }

    @Override
    protected ItemStack copy(ItemStack toCopy) {
        return toCopy.copy();
    }

    @Override
    protected boolean isEmpty(ItemStack value) {
        return value.isEmpty();
    }

    @Override
    protected ContainerType<?, AttachedItems, ?> containerType() {
        return ContainerType.ITEM;
    }

    /**
     * @apiNote Try to minimize the number of calls to this method so that we don't have to look up the data component multiple times.
     */
    @Override
    public ItemStack getStack() {
        return getContents(getAttached());
    }

    @Override
    public ItemResource getResource() {
        return ItemResource.of(getStack());
    }

    @Override
    public int amount() {
        return getStack().count();
    }

    @Override
    public final void setContents(ItemResource itemType, int storedAmount) {
        //TODO - 26.1: Re-evaluate this
        setContents(getAttached(), itemType.toStack(storedAmount));
    }

    /**
     * Ignores current contents
     */
    private boolean isItemValidForInsertion(ItemResource itemType, AutomationType automationType) {
        return isValid(itemType) && canInsert.test(itemType, automationType);
    }

    @Override
    public final int insert(ItemResource resource, int amount, TransactionContext transaction, AutomationType automationType) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        if (amount == 0) {
            //"Fail quick" if the given stack is empty
            return 0;
        }
        AttachedItems attachedItems = getAttached();
        return insertItem(attachedItems, getContents(attachedItems), resource, amount, transaction, automationType);
    }

    public int insertItem(AttachedItems attachedItems, ItemStack current, ItemResource resource, int amount, TransactionContext transaction, AutomationType automationType) {
        if (amount == 0) {
            //"Fail quick" if the given stack is empty
            return 0;
        }
        //Validate that we aren't at max stack size before we try to see if we can insert the item, as on average this will be a cheaper check
        int needed = getLimit(resource) - current.count();
        if (needed <= 0 || !isItemValidForInsertion(resource, automationType)) {
            //Fail if we are a full slot, or we can never insert the item or currently are unable to insert it
            return 0;
        } else if (current.isEmpty() || resource.matches(current)) {
            int toAdd = Math.min(amount, needed);
            updateSnapshots(transaction);
            //Note: We let setStack handle updating the backing holding stack
            // We use current.getCount + toAdd so that if we are empty we end up at toAdd
            // but if we aren't then we grow by the given amount
            setContents(attachedItems, resource.toStack(current.count() + toAdd));
            return toAdd;
        }
        //If we didn't accept this item, then just return the given stack
        return 0;
    }

    @Override
    public int extract(ItemResource resource, int amount, TransactionContext transaction, AutomationType automationType) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        if (amount == 0) {
            //"Fail quick" if nothing is actually being extracted
            return 0;
        }
        AttachedItems attachedItems = getAttached();
        ItemStack current = getContents(attachedItems);
        if (current.isEmpty() || !resource.matches(current) || !canExtract.test(ItemResource.of(current), automationType)) {
            //"Fail quick" if we are empty, a different type is trying to be extracted, or if we can never extract from this slot
            return 0;
        }
        int currentStored = current.count();
        //If we are trying to extract more than we have, just change it so that we are extracting it all
        int toRemove = Math.min(amount, currentStored);
        //Note: We know toRemove is greater than zero so we can just update the snapshot and then set the stack
        updateSnapshots(transaction);
        //Shrink the stack by the amount removed
        setContents(attachedItems, current.copyWithCount(currentStored - toRemove));
        return toRemove;
    }

    @Override
    public int getLimit(ItemResource resource) {
        return obeyStackLimit && !resource.isEmpty() ? Math.min(limit, resource.getMaxStackSize()) : limit;
    }

    @Override
    public boolean isValid(ItemResource itemType) {
        return validator.test(itemType);
    }

    @Override
    public void serialize(ValueOutput output) {
        //TODO - 1.21: This is a copy of BasicInventorySlot#serializeNBT. We might need to also grab the specific overrides of
        // that method as special component backed inventory slots, that then access and put that other data as a different component?
        // Also make sure to override things like TileEntityMekanism#applyInventorySlots and TileEntityMekanism#collectInventorySlots
        ItemStack current = getStack();
        if (!current.isEmpty()) {
            output.store(SerializationConstants.ITEM, SerializerHelper.OVERSIZED_ITEM_CODEC, current);
        }
    }

    @Override
    public void deserialize(ValueInput input) {
        setStack(input.read(SerializationConstants.ITEM, SerializerHelper.OVERSIZED_ITEM_CODEC).orElse(ItemStack.EMPTY));
    }
}