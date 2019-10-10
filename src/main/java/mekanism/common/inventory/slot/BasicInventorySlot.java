package mekanism.common.inventory.slot;

import java.util.function.Predicate;
import javax.annotation.Nonnull;
import mekanism.api.Action;
import mekanism.api.annotations.NonNull;
import mekanism.api.inventory.slot.IInventorySlot;
import mekanism.common.util.StackUtils;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;

public class BasicInventorySlot implements IInventorySlot {

    private static final int DEFAULT_LIMIT = 64;

    @Nonnull
    private final Predicate<@NonNull ItemStack> validator;
    @Nonnull
    private ItemStack current = ItemStack.EMPTY;
    private final Predicate<@NonNull ItemStack> canExtract;
    //TODO: Should the canInsert be removed entirely, as it basically gets covered by the validator
    private final boolean canInsert;
    private final int limit;

    public BasicInventorySlot() {
        this(DEFAULT_LIMIT);
    }

    public BasicInventorySlot(int limit) {
        this(limit, item -> true, true);
    }

    public BasicInventorySlot(Predicate<@NonNull ItemStack> canExtract, boolean canInsert) {
        this(DEFAULT_LIMIT, canExtract, canInsert);
    }

    public BasicInventorySlot(int limit, Predicate<@NonNull ItemStack> canExtract, boolean canInsert) {
        this(limit, canExtract, canInsert, stack -> true);
    }

    public BasicInventorySlot(@Nonnull Predicate<@NonNull ItemStack> validator) {
        this(DEFAULT_LIMIT, validator);
    }

    public BasicInventorySlot(int limit, @Nonnull Predicate<@NonNull ItemStack> validator) {
        this(limit, item -> true, true, validator);
    }

    public BasicInventorySlot(Predicate<@NonNull ItemStack> canExtract, boolean canInsert, @Nonnull Predicate<@NonNull ItemStack> validator) {
        this(DEFAULT_LIMIT, canExtract, canInsert, validator);
    }

    public BasicInventorySlot(int limit, Predicate<@NonNull ItemStack> canExtract, boolean canInsert, @Nonnull Predicate<@NonNull ItemStack> validator) {
        this.limit = limit;
        this.canExtract = canExtract;
        this.canInsert = canInsert;
        this.validator = validator;
    }

    @Nonnull
    @Override
    public ItemStack getStack() {
        //TODO: Should we return a copy to ensure that our stack is not modified, we could cache our copy and only update it at given times
        //TODO: YES it will help expose bugs, and we need to make sure that we are not calling shrink/grow on anything we should not be
        return current;
    }

    @Override
    public void setStack(@Nonnull ItemStack stack) {
        if (stack.isEmpty()) {
            current = ItemStack.EMPTY;
        } else if (isItemValid(stack)) {
            //Limit the max stack size, and copy it to ensure it does not get accidentally modified
            int maxToAccept = Math.min(getLimit(), stack.getMaxStackSize());
            if (stack.getCount() > maxToAccept) {
                //Throws a RuntimeException as IItemHandlerModifiable specifies is allowed when something unexpected happens
                // As setStack is more meant to be used as an internal method
                //TODO: Even if it is valid for this to throw a runtime exception should we be printing an error instead and just refusing to accept the stack
                throw new RuntimeException("Tried to set stack with a size that is larger than this slot accepts.");
            }
            current = StackUtils.size(stack, Math.min(getLimit(), Math.min(stack.getCount(), stack.getMaxStackSize())));
        } else {
            //Throws a RuntimeException as IItemHandlerModifiable specifies is allowed when something unexpected happens
            // As setStack is more meant to be used as an internal method
            //TODO: Even if it is valid for this to throw a runtime exception should we be printing an error instead and just refusing to accept the stack
            throw new RuntimeException("Invalid stack for slot.");
        }
    }

    @Nonnull
    @Override
    public ItemStack insertItem(@Nonnull ItemStack stack, Action action) {
        if (!canInsert() || stack.isEmpty()) {
            //"Fail quick" if we can never insert items or the given stack is empty
            return ItemStack.EMPTY;
        }
        boolean sameType = false;
        if (current.isEmpty() && isItemValid(stack) || (sameType = ItemHandlerHelper.canItemStacksStack(current, stack))) {
            int maxToAdd = stack.getCount();
            //Cap our max size at the limit or the max size of our new stack
            // Note: If we already have a stack then we know it is the same type as our current stack, so the result of getMaxStackSize should be the same
            int maxSize = Math.min(getLimit(), stack.getMaxStackSize());
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
        if (!canExtract() || current.isEmpty() || amount < 1) {
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
        }
        return toReturn;
    }

    @Override
    public int getLimit() {
        //Note: We are not making this be based on the current stored items max stack size, as we limit by that on adding/setting and we are using this
        // for how much can be stored max in this slot at any time
        return limit;
    }

    @Override
    public boolean isItemValid(@Nonnull ItemStack stack) {
        return validator.test(stack);
    }

    protected boolean canInsert() {
        return canInsert;
    }

    protected boolean canExtract() {
        return canExtract.test(current);
    }
}