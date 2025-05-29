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
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

//TODO: Should we make some sort of "ITickableSlot" or something that lets us tick a bunch of slots at once instead of having to manually call the relevant methods
@NothingNullByDefault
public class BasicInventorySlot implements IInventorySlot {

    public static BasicInventorySlot at(@Nullable IContentsListener listener, int x, int y) {
        return at(ConstantPredicates.alwaysTrue(), listener, x, y);
    }

    public static BasicInventorySlot at(Predicate<@NotNull ItemStack> validator, @Nullable IContentsListener listener, int x, int y) {
        return at(validator, listener, x, y, Item.ABSOLUTE_MAX_STACK_SIZE);
    }

    public static BasicInventorySlot at(Predicate<@NotNull ItemStack> validator, @Nullable IContentsListener listener, int x, int y, int limit) {
        Objects.requireNonNull(validator, "Item validity check cannot be null");
        if (limit < 1) {
            throw new IllegalArgumentException("Slots with a custom limit must allow at least one item");
        }
        return new BasicInventorySlot(limit, ConstantPredicates.alwaysTrueBi(), ConstantPredicates.alwaysTrueBi(), validator, listener, x, y);
    }

    public static BasicInventorySlot at(Predicate<@NotNull ItemStack> canExtract, Predicate<@NotNull ItemStack> canInsert, @Nullable IContentsListener listener, int x, int y) {
        Objects.requireNonNull(canExtract, "Extraction validity check cannot be null");
        Objects.requireNonNull(canInsert, "Insertion validity check cannot be null");
        return new BasicInventorySlot(canExtract, canInsert, ConstantPredicates.alwaysTrue(), listener, x, y);
    }

    public static BasicInventorySlot at(BiPredicate<@NotNull ItemStack, @NotNull AutomationType> canExtract,
          BiPredicate<@NotNull ItemStack, @NotNull AutomationType> canInsert, @Nullable IContentsListener listener, int x, int y) {
        return at(canExtract, canInsert, ConstantPredicates.alwaysTrue(), listener, x, y);
    }

    public static BasicInventorySlot at(BiPredicate<@NotNull ItemStack, @NotNull AutomationType> canExtract,
          BiPredicate<@NotNull ItemStack, @NotNull AutomationType> canInsert, Predicate<@NotNull ItemStack> validator, @Nullable IContentsListener listener, int x, int y) {
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
    private final BiPredicate<@NotNull ItemStack, @NotNull AutomationType> canExtract;
    private final BiPredicate<@NotNull ItemStack, @NotNull AutomationType> canInsert;
    private final Predicate<@NotNull ItemStack> validator;
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

    protected BasicInventorySlot(Predicate<@NotNull ItemStack> canExtract, Predicate<@NotNull ItemStack> canInsert, Predicate<@NotNull ItemStack> validator,
          @Nullable IContentsListener listener, int x, int y) {
        this((stack, automationType) -> automationType == AutomationType.MANUAL || canExtract.test(stack), (stack, automationType) -> canInsert.test(stack),
              validator, listener, x, y);
    }

    protected BasicInventorySlot(BiPredicate<@NotNull ItemStack, @NotNull AutomationType> canExtract, BiPredicate<@NotNull ItemStack, @NotNull AutomationType> canInsert,
          Predicate<@NotNull ItemStack> validator, @Nullable IContentsListener listener, int x, int y) {
        this(Item.ABSOLUTE_MAX_STACK_SIZE, canExtract, canInsert, validator, listener, x, y);
    }

    protected BasicInventorySlot(int limit, BiPredicate<@NotNull ItemStack, @NotNull AutomationType> canExtract,
          BiPredicate<@NotNull ItemStack, @NotNull AutomationType> canInsert, Predicate<@NotNull ItemStack> validator, @Nullable IContentsListener listener, int x, int y) {
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
        } else if (!validateStack || isItemValid(stack)) {
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
        int needed = getLimit(stack) - current.getCount();
        if (needed <= 0 || !isItemValidForInsertion(stack, automationType)) {
            //Fail if we are a full slot, or we can never insert the item or currently are unable to insert it
            return stack;
        }
        boolean sameType = false;
        if (current.isEmpty() || (sameType = ItemStack.isSameItemSameComponents(current, stack))) {
            int toAdd = Math.min(stack.getCount(), needed);
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
            return stack.copyWithCount(stack.getCount() - toAdd);
        }
        //If we didn't accept this item, then just return the given stack
        return stack;
    }

    @Override
    public ItemStack extractItem(int amount, Action action, AutomationType automationType) {
        if (current.isEmpty() || amount < 1 || !canExtract.test(current, automationType)) {
            //"Fail quick" if we don't can never extract from this slot, have an item stored, or the amount being requested is less than one
            return ItemStack.EMPTY;
        }
        //Ensure that if this slot allows going past the max stack size of an item, that when extracting we don't act as if we have more than
        // the max stack size, as the JavaDoc for IItemHandler requires that the returned stack is not larger than its stack size
        int currentAmount = Math.min(current.getCount(), current.getMaxStackSize());
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
    public boolean isItemValid(ItemStack stack) {
        return validator.test(stack);
    }

    /**
     * Ignores current contents
     */
    public boolean isItemValidForInsertion(ItemStack stack, AutomationType automationType) {
        return validator.test(stack) && canInsert.test(stack, automationType);
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

    /**
     * @implNote Overwritten so that if we decide to change to returning a cached/copy of our stack in {@link #getStack()}, we can optimize out the copying, and can also
     * directly modify our stack instead of having to make a copy.
     */
    @Override
    public int setStackSize(int amount, Action action) {
        if (current.isEmpty()) {
            return 0;
        } else if (amount <= 0) {
            if (action.execute()) {
                setEmpty();
            }
            return 0;
        }
        int maxStackSize = getLimit(current);
        if (amount > maxStackSize) {
            amount = maxStackSize;
        }
        if (current.getCount() == amount || action.simulate()) {
            //If our size is not changing, or we are only simulating the change, don't do anything
            return amount;
        }
        current.setCount(amount);
        onContentsChanged();
        return amount;
    }

    /**
     * @implNote Overwritten so that if we decide to change to returning a cached/copy of our stack in {@link #getStack()}, we can optimize out the copying.
     */
    @Override
    public int growStack(int amount, Action action) {
        int current = this.current.getCount();
        if (current == 0) {
            //"Fail quick" if our stack is empty, so we can't grow it
            return 0;
        } else if (amount > 0) {
            //Cap adding amount at how much we need, so that we don't risk integer overflow
            amount = Math.min(amount, getLimit(this.current));
        }
        int newSize = setStackSize(current + amount, action);
        return newSize - current;
    }

    /**
     * @implNote Overwritten so that if we decide to change to returning a cached/copy of our stack in {@link #getStack()}, we can optimize out the copying.
     */
    @Override
    public boolean isEmpty() {
        return current.isEmpty();
    }

    /**
     * @implNote Overwritten so that if we decide to change to returning a cached/copy of our stack in {@link #getStack()}, we can optimize out the copying.
     */
    @Override
    public int getCount() {
        return current.getCount();
    }

    /**
     * @implNote Overwritten so that if we decide to change to returning a cached/copy of our stack in {@link #getStack()}, we can optimize out the copying.
     */
    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag nbt = new CompoundTag();
        if (!isEmpty()) {
            nbt.put(SerializationConstants.ITEM, SerializerHelper.saveOversized(provider, current));
        }
        return nbt;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        //Set the stack in an unchecked way so that if it is no longer valid, we don't end up
        // crashing due to the stack not being valid
        setStackUnchecked(SerializerHelper.parseOversizedOptional(provider, nbt.getCompound(SerializationConstants.ITEM)));
    }
}