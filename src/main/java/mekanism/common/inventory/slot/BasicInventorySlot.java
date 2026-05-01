package mekanism.common.inventory.slot;

import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;
import mekanism.api.Action;
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
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStackResourceHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

//TODO: Should we make some sort of "ITickableSlot" or something that lets us tick a bunch of slots at once instead of having to manually call the relevant methods
@NothingNullByDefault
public class BasicInventorySlot implements IInventorySlot {

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

    /**
     * @apiNote This is only protected for direct querying access. To modify this stack the external methods or {@link #setStackUnchecked(ItemStack)} should be used
     * instead.
     */
    protected ItemStack current = ItemStack.EMPTY;
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
    public ItemStack getStack() {
        return current;
    }

    @Override
    public void setStack(ItemStack stack) {
        setStack(stack, true);
    }

    public void setStackUnchecked(ItemStack stack) {
        setStack(stack, false);
    }

    private void setStack(ItemStack stack, boolean validateStack) {
        if (stack.isEmpty()) {
            if (current.isEmpty()) {
                //If we are already empty just exit, to not fire onContentsChanged
                return;
            }
            current = ItemStack.EMPTY;
        } else if (!validateStack || isValid(ItemResource.of(stack))) {
            current = stack.copy();
        } else {
            //Throws a RuntimeException as IItemHandlerModifiable specifies is allowed when something unexpected happens
            // As setStack is more meant to be used as an internal method
            throw new RuntimeException("Invalid stack for slot: " + stack + " " + stack.getComponentsPatch());
        }
        onContentsChanged();
    }

    @Override
    public ItemStack insertItem(ItemStack stack, Action action, AutomationType automationType) {
        if (stack.isEmpty()) {
            //"Fail quick" if the given stack is empty
            return ItemStack.EMPTY;
        }
        //Validate that we aren't at max stack size before we try to see if we can insert the item, as on average this will be a cheaper check
        int needed = getLimit(stack) - current.count();
        if (needed <= 0 || !isItemValidForInsertion(stack, automationType)) {
            //Fail if we are a full slot, or we can never insert the item or currently are unable to insert it
            return stack;
        }
        boolean sameType = false;
        if (current.isEmpty() || (sameType = ItemStack.isSameItemSameComponents(current, stack))) {
            int toAdd = Math.min(stack.count(), needed);
            if (action.execute()) {
                //If we want to actually insert the item, then update the current item
                if (sameType) {
                    //We can just grow our stack by the amount we want to increase it
                    current.grow(toAdd);
                    onContentsChanged();
                } else {
                    //If we are not the same type then we have to copy the stack and set it
                    // Just set it unchecked as we have already validated it
                    // Note: this also will mark that the contents changed
                    setStackUnchecked(stack.copyWithCount(toAdd));
                }
            }
            return stack.copyWithCount(stack.count() - toAdd);
        }
        //If we didn't accept this item, then just return the given stack
        return stack;
    }

    @Override
    public ItemStack extractItem(int amount, Action action, AutomationType automationType) {
        if (current.isEmpty() || amount < 1 || !canExtract.test(ItemResource.of(current), automationType)) {
            //"Fail quick" if we don't can never extract from this slot, have an item stored, or the amount being requested is less than one
            return ItemStack.EMPTY;
        }
        //Ensure that if this slot allows going past the max stack size of an item, that when extracting we don't act as if we have more than
        // the max stack size, as the JavaDoc for IItemHandler requires that the returned stack is not larger than its stack size
        int currentAmount = Math.min(getCount(), current.getMaxStackSize());
        if (currentAmount < amount) {
            //If we are trying to extract more than we have, just change it so that we are extracting it all
            amount = currentAmount;
        }
        //Note: While we technically could just return the stack itself if we are removing all that we have, it would require a lot more checks
        // especially for supporting the fact of limiting by the max stack size.
        ItemStack toReturn = current.copyWithCount(amount);
        if (action.execute()) {
            //If shrink gets the size to zero it will update the empty state so that isEmpty() returns true.
            current.shrink(amount);
            onContentsChanged();
        }
        return toReturn;
    }

    @Override
    public int getLimit(ItemStack stack) {
        return obeyStackLimit && !stack.isEmpty() ? Math.min(limit, stack.getMaxStackSize()) : limit;
    }

    @Override
    public boolean isValid(ItemResource itemType) {
        return validator.test(itemType);
    }

    /**
     * Ignores current contents
     */
    public boolean isItemValidForInsertion(ItemStack stack, AutomationType automationType) {
        ItemResource itemType = ItemResource.of(stack);
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