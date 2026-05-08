package mekanism.common.attachments.containers.item;

import com.google.common.primitives.Ints;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.SerializationConstants;
import mekanism.api.SerializerHelper;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.attachments.containers.ComponentBackedResourceContainer;
import mekanism.common.attachments.containers.ContainerType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jetbrains.annotations.NotNull;

@NothingNullByDefault
public class ComponentBackedInventorySlot extends ComponentBackedResourceContainer<ItemResource, ItemStack, AttachedItems> implements IInventorySlot {

    private final boolean obeyStackLimit;

    public ComponentBackedInventorySlot(ItemStack attachedTo, int slotIndex, BiPredicate<ItemResource, AutomationType> canExtract,
          BiPredicate<ItemResource, AutomationType> canInsert, Predicate<@NotNull ItemResource> validator) {
        this(attachedTo, slotIndex, canExtract, canInsert, validator, true, Item.ABSOLUTE_MAX_STACK_SIZE);
    }

    public ComponentBackedInventorySlot(ItemStack attachedTo, int slotIndex, BiPredicate<ItemResource, AutomationType> canExtract,
          BiPredicate<ItemResource, AutomationType> canInsert, Predicate<@NotNull ItemResource> validator, boolean obeyStackLimit, int limit) {
        super(attachedTo, slotIndex, limit, canExtract, canInsert, validator);
        this.obeyStackLimit = obeyStackLimit;
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
    protected ItemResource asResource(ItemStack stack) {
        return ItemResource.of(stack);
    }

    @Override
    protected long getAmountAsLong(ItemStack stack) {
        return stack.count();
    }

    @Override
    protected void setContents(AttachedItems attachedItems, ItemResource type, long storedAmount) {
        //TODO - 26.1: Change it to using a LargeResourceStack for the contents so that it can support long amounts
        setContents(attachedItems, type.toStack(Ints.saturatedCast(storedAmount)));
    }

    @Override
    public long getLimitAsLong(ItemResource resource) {
        long limit = super.getLimitAsLong(resource);
        return obeyStackLimit && !resource.isEmpty() ? Math.min(limit, resource.getMaxStackSize()) : limit;
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