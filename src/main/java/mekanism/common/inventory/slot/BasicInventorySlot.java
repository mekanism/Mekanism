package mekanism.common.inventory.slot;

import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.SerializationConstants;
import mekanism.api.SerializerHelper;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.inventory.container.slot.ContainerSlotType;
import mekanism.common.inventory.container.slot.InventoryContainerSlot;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.inventory.warning.ISupportsWarning;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStackResourceHandler;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

//TODO: Should we make some sort of "ITickableSlot" or something that lets us tick a bunch of slots at once instead of having to manually call the relevant methods
@NothingNullByDefault
public class BasicInventorySlot extends SnapshotJournal<ItemStack> implements IInventorySlot {//TODO - 26.1: Docs on how this is similar to ItemStackResourceHandler

    private final ItemAccess itemAccess = ItemAccess.forHandlerIndex(new ResourceHandlerWrapper(), 0);

    public static BasicInventorySlot at(@Nullable IContentsListener listener, int x, int y) {
        return at(ConstantPredicates.alwaysTrue(), listener, x, y);
    }

    public static BasicInventorySlot at(Predicate<@NotNull ItemResource> validator, @Nullable IContentsListener listener, int x, int y) {
        return at(validator, listener, x, y, Item.ABSOLUTE_MAX_STACK_SIZE);
    }

    public static BasicInventorySlot at(Predicate<@NotNull ItemResource> validator, @Nullable IContentsListener listener, int x, int y, int limit) {
        Objects.requireNonNull(validator, "Item validity check cannot be null");
        if (limit < 1) {
            throw new IllegalArgumentException("Slots with a custom limit must allow at least one item");
        }
        return new BasicInventorySlot(limit, ConstantPredicates.alwaysTrueBi(), ConstantPredicates.alwaysTrueBi(), validator, listener, x, y);
    }

    public static BasicInventorySlot at(Predicate<@NotNull ItemResource> canExtract, Predicate<@NotNull ItemResource> canInsert, @Nullable IContentsListener listener, int x, int y) {
        Objects.requireNonNull(canExtract, "Extraction validity check cannot be null");
        Objects.requireNonNull(canInsert, "Insertion validity check cannot be null");
        return new BasicInventorySlot(canExtract, canInsert, ConstantPredicates.alwaysTrue(), listener, x, y);
    }

    public static BasicInventorySlot at(BiPredicate<@NotNull ItemResource, @NotNull AutomationType> canExtract,
          BiPredicate<@NotNull ItemResource, @NotNull AutomationType> canInsert, @Nullable IContentsListener listener, int x, int y) {
        return at(canExtract, canInsert, ConstantPredicates.alwaysTrue(), listener, x, y);
    }

    public static BasicInventorySlot at(BiPredicate<@NotNull ItemResource, @NotNull AutomationType> canExtract, BiPredicate<@NotNull ItemResource, @NotNull AutomationType> canInsert,
          Predicate<@NotNull ItemResource> validator, @Nullable IContentsListener listener, int x, int y) {
        Objects.requireNonNull(canExtract, "Extraction validity check cannot be null");
        Objects.requireNonNull(canInsert, "Insertion validity check cannot be null");
        Objects.requireNonNull(validator, "Item validity check cannot be null");
        return new BasicInventorySlot(canExtract, canInsert, validator, listener, x, y);
    }

    private final BiPredicate<ItemResource, AutomationType> canExtract;
    private final BiPredicate<ItemResource, AutomationType> canInsert;
    private final Predicate<ItemResource> validator;
    private final int limit;
    @Nullable
    private final IContentsListener listener;
    private final int x;
    private final int y;
    protected boolean obeyStackLimit = true;
    private ContainerSlotType slotType = ContainerSlotType.NORMAL;
    @Nullable
    private SlotOverlay slotOverlay;
    @Nullable
    private Consumer<ISupportsWarning<?>> warningAdder;

    private ItemResource currentType = ItemResource.EMPTY;
    private int storedAmount = 0;

    protected BasicInventorySlot(Predicate<ItemResource> canExtract, Predicate<ItemResource> canInsert, Predicate<ItemResource> validator,
          @Nullable IContentsListener listener, int x, int y) {
        this((itemType, automationType) -> automationType == AutomationType.MANUAL || canExtract.test(itemType),
              (itemType, _) -> canInsert.test(itemType), validator, listener, x, y);
    }

    protected BasicInventorySlot(BiPredicate<ItemResource, AutomationType> canExtract, BiPredicate<ItemResource, AutomationType> canInsert,
          Predicate<ItemResource> validator, @Nullable IContentsListener listener, int x, int y) {
        this(Item.ABSOLUTE_MAX_STACK_SIZE, canExtract, canInsert, validator, listener, x, y);
    }

    protected BasicInventorySlot(int limit, BiPredicate<ItemResource, AutomationType> canExtract, BiPredicate<ItemResource, AutomationType> canInsert,
          Predicate<ItemResource> validator, @Nullable IContentsListener listener, int x, int y) {
        this.limit = limit;
        this.canExtract = canExtract;
        this.canInsert = canInsert;
        this.validator = validator;
        this.listener = listener;
        this.x = x;
        this.y = y;
    }

    public int getGuiX() {
        return x;
    }

    @Override
    public ItemResource getResource() {
        return this.currentType;
    }

    @Override
    public int getCount() {
        return storedAmount;
    }

    @Override
    public void setStack(ItemResource itemType, int storedAmount) {
        setStack(itemType, storedAmount, true);
    }

    @Deprecated(forRemoval = true)//TODO - 26.1: Move calls to setStackUnchecked(ItemResource, int)
    public void setStackUnchecked(ItemStack stack) {
        setStackUnchecked(ItemResource.of(stack), stack.count());
    }

    public void setStackUnchecked(ItemResource itemType, int storedAmount) {
        setStack(itemType, storedAmount, false);
    }

    private void setStack(ItemResource itemType, int storedAmount, boolean validateStack) {
        TransferPreconditions.checkNonNegative(storedAmount);
        if (itemType.isEmpty() || storedAmount == 0) {//TODO - 26.1: Make sure that storedAmount can never have a negative passed,
            if (isEmpty()) {
                //If we are already empty just exit, to not fire onContentsChanged
                return;
            }
            this.currentType = ItemResource.EMPTY;
            this.storedAmount = 0;
        } else if (!validateStack || isValid(itemType)) {
            this.currentType = itemType;
            this.storedAmount = storedAmount;
        } else {
            //Throws a RuntimeException as IItemHandlerModifiable specifies is allowed when something unexpected happens
            // As setStack is more meant to be used as an internal method
            //TODO - 26.1: Evaluate if we still want to be throwing an exception
            throw new RuntimeException("Invalid stack for slot: " + itemType.value() + " " + itemType.getComponentsPatch());
        }
        //TODO - 26.1: Delay this until the transactions are committed when setting from a transactional context (some things like setting from slots isn't transactional)
        onContentsChanged();
    }

    @Override
    public int insert(ItemResource resource, int amount, TransactionContext transaction, AutomationType automationType) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        if (amount == 0) {
            //"Fail quick" if the given stack is empty
            return 0;
        }
        int currentStored = getCount();
        //Validate that we aren't at max stack size before we try to see if we can insert the item, as on average this will be a cheaper check
        int needed = getLimit(resource) - currentStored;
        if (needed <= 0 || !isValidForInsertion(resource, automationType)) {
            //Fail if we are a full slot, or we can never insert the item or currently are unable to insert it
            return 0;
        } else if (!isEmpty() && !this.currentType.equals(resource)) {
            //Fail if the type being inserted doesn't match our current stored type
            //TODO - 26.1: Re-evaluate if this should be above the isValidForInsertion check
            return 0;
        }
        int toAdd = Math.min(amount, needed);
        //Note: We know toAdd is greater than zero so we can just update the snapshot and then set the stack
        updateSnapshots(transaction);
        // Note: We just set it as unchecked as we have already validated it
        setStackUnchecked(resource, currentStored + toAdd);
        return toAdd;
    }

    @Override
    public int extract(ItemResource resource, int amount, TransactionContext transaction, AutomationType automationType) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        if (isEmpty() || amount == 0 || !this.currentType.equals(resource) || !isCurrentValidForExtraction(automationType)) {
            //"Fail quick" if we are empty, nothing is being extracted, a different type is trying to be extracted, or if we can never extract from this slot
            return 0;
        }
        int currentStored = getCount();
        //If we are trying to extract more than we have, just change it so that we are extracting it all
        int toRemove = Math.min(amount, currentStored);
        //Note: We know toRemove is greater than zero so we can just update the snapshot and then set the stack
        updateSnapshots(transaction);
        //Shrink the stack by the amount removed
        setStackUnchecked(resource, currentStored - toRemove);
        //TODO - 26.1: Shrink and make shrink transactional?
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

    /**
     * Ignores current contents
     */
    public boolean isCurrentValidForExtraction(AutomationType automationType) {
        return canExtract.test(this.currentType, automationType);
    }

    /**
     * Ignores current contents
     */
    public boolean isValidForInsertion(ItemResource itemType, AutomationType automationType) {
        return isValid(itemType) && canInsert.test(itemType, automationType);
    }

    @Override
    public void onContentsChanged() {
        if (listener != null) {
            listener.onContentsChanged();
        }
    }

    @Nullable
    @Override
    public InventoryContainerSlot createContainerSlot() {
        return new InventoryContainerSlot(this, x, y, slotType, slotOverlay, warningAdder, this::setStackUnchecked);
    }

    public void setSlotType(ContainerSlotType slotType) {
        //TODO - 1.18: Re-evaluate this method as for the most part we now seem to be handling this in GuiMekanism
        // and figuring it out based on the data type; which at the very least means we can probably remove some
        // calls to this. Though there are also some cases where we want to override it where it doesn't now as
        // the fallback sets it to normal basically regardless (see evaporation multiblock and input slots)
        this.slotType = slotType;
    }

    public void tracksWarnings(@Nullable Consumer<ISupportsWarning<?>> warningAdder) {
        this.warningAdder = warningAdder;
    }

    public void setSlotOverlay(@Nullable SlotOverlay slotOverlay) {
        this.slotOverlay = slotOverlay;
    }

    @Nullable
    protected final SlotOverlay getSlotOverlay() {
        return slotOverlay;
    }

    protected final ContainerSlotType getSlotType() {
        return slotType;
    }

    @Override
    public void deserialize(ValueInput input) {
        //Set the stack in an unchecked way so that if it is no longer valid, we don't end up
        // crashing due to the stack not being valid
        setStackUnchecked(input.read(SerializationConstants.ITEM, SerializerHelper.OVERSIZED_ITEM_CODEC).orElse(ItemStack.EMPTY));
    }

    //TODO - 26.1: review this
    public ItemAccess itemAccess() {
        return itemAccess;
    }

    @Override
    protected ItemStack createSnapshot() {
        return this.currentType.toStack(this.storedAmount);
    }

    @Override
    protected void revertToSnapshot(ItemStack snapshot) {
        setStackUnchecked(ItemResource.of(snapshot), snapshot.count());
    }

    @Override
    protected void onRootCommit(ItemStack originalState) {
        if (this.storedAmount != originalState.count() || !this.currentType.matches(originalState)) {
            //Fire content change listeners during root commit if the final state is different from the original one
            onContentsChanged();
        }
    }

    private class ResourceHandlerWrapper extends ItemStackResourceHandler {

        @Override
        protected ItemStack getStack() {
            return BasicInventorySlot.this.getStack().copy();
        }

        @Override
        public long getAmountAsLong(int index) {
            Objects.checkIndex(index, 1);
            return BasicInventorySlot.this.getCount();
        }

        @Override
        protected void setStack(ItemStack stack) {
            BasicInventorySlot.this.setStackUnchecked(stack);
        }

        @Override
        protected boolean isValid(ItemResource resource) {
            return BasicInventorySlot.this.isValid(resource);
        }
    }
}