package mekanism.common.inventory.slot;

import java.util.function.Predicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Action;
import mekanism.api.annotations.NonNull;
import mekanism.api.inventory.slot.IInventorySlot;
import mekanism.common.inventory.container.slot.ContainerSlotType;
import mekanism.common.inventory.container.slot.InventoryContainerSlot;
import mekanism.common.util.StackUtils;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;

public class BasicInventorySlot implements IInventorySlot {

    protected static final Predicate<@NonNull ItemStack> alwaysTrue = item -> true;
    protected static final Predicate<@NonNull ItemStack> alwaysFalse = item -> false;
    private static final int DEFAULT_LIMIT = 64;

    public static BasicInventorySlot at(int x, int y) {
        return new BasicInventorySlot(x, y);
    }

    public static BasicInventorySlot at(@Nonnull Predicate<@NonNull ItemStack> validator, int x, int y) {
        return new BasicInventorySlot(validator, x, y);
    }

    public static BasicInventorySlot at(Predicate<@NonNull ItemStack> canExtract, Predicate<@NonNull ItemStack> canInsert, int x, int y) {
        return new BasicInventorySlot(canExtract, canInsert, x, y);
    }

    @Nonnull
    private final Predicate<@NonNull ItemStack> validator;
    @Nonnull
    private ItemStack current = ItemStack.EMPTY;
    private final Predicate<@NonNull ItemStack> canExtract;
    private final Predicate<@NonNull ItemStack> canInsert;
    private final int limit;
    protected final int x;
    protected final int y;

    //TODO: Make these protected and maybe remove some of these default helper constructors
    public BasicInventorySlot(int x, int y) {
        this(DEFAULT_LIMIT, x, y);
    }

    public BasicInventorySlot(int limit, int x, int y) {
        this(limit, alwaysTrue, alwaysTrue, x, y);
    }

    public BasicInventorySlot(Predicate<@NonNull ItemStack> canExtract, Predicate<@NonNull ItemStack> canInsert, int x, int y) {
        this(DEFAULT_LIMIT, canExtract, canInsert, x, y);
    }

    public BasicInventorySlot(int limit, Predicate<@NonNull ItemStack> canExtract, Predicate<@NonNull ItemStack> canInsert, int x, int y) {
        this(limit, canExtract, canInsert, alwaysTrue, x, y);
    }

    public BasicInventorySlot(@Nonnull Predicate<@NonNull ItemStack> validator, int x, int y) {
        this(DEFAULT_LIMIT, validator, x, y);
    }

    public BasicInventorySlot(int limit, @Nonnull Predicate<@NonNull ItemStack> validator, int x, int y) {
        this(limit, alwaysTrue, alwaysTrue, validator, x, y);
    }

    public BasicInventorySlot(Predicate<@NonNull ItemStack> canExtract, Predicate<@NonNull ItemStack> canInsert, @Nonnull Predicate<@NonNull ItemStack> validator,
          int x, int y) {
        this(DEFAULT_LIMIT, canExtract, canInsert, validator, x, y);
    }

    public BasicInventorySlot(int limit, Predicate<@NonNull ItemStack> canExtract, Predicate<@NonNull ItemStack> canInsert,
          @Nonnull Predicate<@NonNull ItemStack> validator, int x, int y) {
        this.limit = limit;
        this.canExtract = canExtract;
        this.canInsert = canInsert;
        this.validator = validator;
        this.x = x;
        this.y = y;
    }

    /**
     * {@inheritDoc}
     *
     * @apiNote We return a cached value from this that if modified won't actually end up having any information about the slot get changed.
     */
    @Nonnull
    @Override
    public ItemStack getStack() {
        //TODO: Should we return a copy to ensure that our stack is not modified, we could cache our copy and only update it at given times
        //TODO: YES it will help expose bugs, and we need to make sure that we are not calling shrink/grow on anything we should not be
        // Though it would be "cleaner" to not have to especially in terms of for finding bugs when API is being mistreated.
        // Would be nice to extend ItemStack to have one that throws a warning/error on being modified
        return current;
    }

    @Override
    public void setStack(@Nonnull ItemStack stack) {
        if (stack.isEmpty()) {
            current = ItemStack.EMPTY;
        } else if (isItemValid(stack)) {
            //Limit the max stack size, and copy it to ensure it does not get accidentally modified
            int maxToAccept = getStackLimit(stack);
            if (stack.getCount() > maxToAccept) {
                //Throws a RuntimeException as IItemHandlerModifiable specifies is allowed when something unexpected happens
                // As setStack is more meant to be used as an internal method
                //TODO: Even if it is valid for this to throw a runtime exception should we be printing an error instead and just refusing to accept the stack
                throw new RuntimeException("Tried to set stack with a size that is larger than this slot accepts.");
            }
            //Note: While technically we don't need to update it if the stack we are setting it to is identical, the logic is a lot simpler to do so
            // and in most cases that case will not happen anyways
            current = StackUtils.size(stack, Math.min(stack.getCount(), maxToAccept));
        } else {
            //Throws a RuntimeException as IItemHandlerModifiable specifies is allowed when something unexpected happens
            // As setStack is more meant to be used as an internal method
            //TODO: Even if it is valid for this to throw a runtime exception should we be printing an error instead and just refusing to accept the stack
            throw new RuntimeException("Invalid stack for slot.");
        }
        onContentsChanged();
    }

    @Nonnull
    @Override
    public ItemStack insertItem(@Nonnull ItemStack stack, Action action) {
        if (stack.isEmpty() || !isItemValid(stack) || !canInsert.test(stack)) {
            //"Fail quick" if the given stack is empty or we can never insert the item or currently are unable to insert it
            return ItemStack.EMPTY;
        }
        boolean sameType = false;
        if (current.isEmpty() || (sameType = ItemHandlerHelper.canItemStacksStack(current, stack))) {
            int maxToAdd = stack.getCount();
            //Cap our max size at the limit or the max size of our new stack
            // Note: If we already have a stack then we know it is the same type as our current stack, so the result of getMaxStackSize should be the same
            int maxSize = getStackLimit(stack);
            if (maxToAdd < maxSize) {
                int toAdd = maxSize - maxToAdd;
                if (action.execute()) {
                    //If we want to actually insert the item, then update the current item
                    if (sameType) {
                        //We can just grow our stack by the amount we want to increase it
                        current.grow(toAdd);
                    } else {
                        //If we are not the same type then we have to copy the stack and set it
                        current = StackUtils.size(stack, toAdd);
                    }
                    onContentsChanged();
                }
                return StackUtils.size(stack, maxToAdd - toAdd);
            }
        }
        //If we didn't accept this item, then just return the given stack
        return stack;
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int amount, Action action) {
        if (current.isEmpty() || amount < 1 || !canExtract.test(current)) {
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
        ItemStack toReturn = StackUtils.size(current, amount);
        if (action.execute()) {
            //If shrink gets the size to zero it will update the empty state so that current.isEmpty() returns true.
            current.shrink(amount);
            onContentsChanged();
        }
        return toReturn;
    }

    @Override
    public int getLimit() {
        return limit;
    }

    @Override
    public boolean isItemValid(@Nonnull ItemStack stack) {
        return validator.test(stack);
    }

    @Override
    public void onContentsChanged() {
        //TODO: IMPLEMENT THIS so as ot mark the tile/inventory it is in as dirty
    }

    //TODO: Should we move InventoryContainerSlot to the API and reference that instead
    @Nullable
    @Override
    public InventoryContainerSlot createContainerSlot(int index) {
        return new InventoryContainerSlot(this, index, x, y, getSlotType());
    }

    //TODO: Implement this properly in the different subclasses/slot types
    protected ContainerSlotType getSlotType() {
        return ContainerSlotType.NORMAL;
    }

    /**
     * {@inheritDoc}
     *
     * @implNote Overwritten as we return a cached/copy of our stack in {@link #getStack()}, and we can optimize out the copying.
     */
    @Override
    public int setStackSize(int amount) {
        if (current.isEmpty()) {
            return 0;
        }
        if (amount <= 0) {
            setStack(ItemStack.EMPTY);
            return 0;
        }
        int maxStackSize = getStackLimit();
        if (amount > maxStackSize) {
            amount = maxStackSize;
        }
        if (current.getCount() == amount) {
            //If our size is not changing don't do anything
            return amount;
        }
        current.setCount(amount);
        onContentsChanged();
        return amount;
    }

    /**
     * {@inheritDoc}
     *
     * @implNote Overwritten as we return a cached/copy of our stack in {@link #getStack()}, and we can optimize out the copying.
     */
    @Override
    public int growStack(int amount) {
        int currentCount = current.getCount();
        int newSize = setStackSize(currentCount + amount);
        return newSize - currentCount;
    }

    /**
     * {@inheritDoc}
     *
     * @implNote Overwritten as we return a cached/copy of our stack in {@link #getStack()}, and we can optimize out the copying.
     */
    @Override
    public boolean isEmpty() {
        return current.isEmpty();
    }

    /**
     * {@inheritDoc}
     *
     * @implNote Overwritten as we return a cached/copy of our stack in {@link #getStack()}, and we can optimize out the copying.
     */
    @Override
    public int getStackLimit() {
        return getStackLimit(current);
    }
}