package mekanism.common.inventory.slot;

import java.util.function.Predicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Action;
import mekanism.api.annotations.NonNull;
import mekanism.api.inventory.IMekanismInventory;
import mekanism.api.inventory.slot.IInventorySlot;
import mekanism.common.inventory.container.slot.ContainerSlotType;
import mekanism.common.inventory.container.slot.InventoryContainerSlot;
import mekanism.common.util.StackUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.items.ItemHandlerHelper;

public class BasicInventorySlot implements IInventorySlot {

    protected static final Predicate<@NonNull ItemStack> alwaysTrue = stack -> true;
    protected static final Predicate<@NonNull ItemStack> alwaysFalse = stack -> false;
    private static final int DEFAULT_LIMIT = 64;

    public static BasicInventorySlot at(IMekanismInventory inventory, int x, int y) {
        return at(alwaysTrue, inventory, x, y);
    }

    public static BasicInventorySlot at(@Nonnull Predicate<@NonNull ItemStack> validator, IMekanismInventory inventory, int x, int y) {
        return new BasicInventorySlot(alwaysTrue, alwaysTrue, validator, inventory, x, y);
    }

    public static BasicInventorySlot at(Predicate<@NonNull ItemStack> canExtract, Predicate<@NonNull ItemStack> canInsert, IMekanismInventory inventory, int x, int y) {
        return new BasicInventorySlot(canExtract, canInsert, alwaysTrue, inventory, x, y);
    }

    public static BasicInventorySlot at(int limit, Predicate<@NonNull ItemStack> canExtract, Predicate<@NonNull ItemStack> canInsert, IMekanismInventory inventory,
          int x, int y) {
        return new BasicInventorySlot(limit, canExtract, canInsert, alwaysTrue, inventory, x, y);
    }

    @Nonnull
    private final Predicate<@NonNull ItemStack> validator;
    @Nonnull
    private ItemStack current = ItemStack.EMPTY;
    private final Predicate<@NonNull ItemStack> canExtract;
    private final Predicate<@NonNull ItemStack> canInsert;
    private final int limit;
    private final IMekanismInventory inventory;
    private final int x;
    private final int y;
    protected boolean obeyStackLimit = true;

    protected BasicInventorySlot(Predicate<@NonNull ItemStack> canExtract, Predicate<@NonNull ItemStack> canInsert, @Nonnull Predicate<@NonNull ItemStack> validator,
          IMekanismInventory inventory, int x, int y) {
        this(DEFAULT_LIMIT, canExtract, canInsert, validator, inventory, x, y);
    }

    protected BasicInventorySlot(int limit, Predicate<@NonNull ItemStack> canExtract, Predicate<@NonNull ItemStack> canInsert,
          @Nonnull Predicate<@NonNull ItemStack> validator, IMekanismInventory inventory, int x, int y) {
        this.limit = limit;
        this.canExtract = canExtract;
        this.canInsert = canInsert;
        this.validator = validator;
        this.inventory = inventory;
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
        //TODO: Decide if we want to limit this to the slots limit and maybe make a method for reading from file that lets it go past the limit??
        if (stack.isEmpty()) {
            current = ItemStack.EMPTY;
        } else if (isItemValid(stack)) {
            //TODO: Should we allow forcefully setting invalid items? At least we need to go through them and check to make sure we allow setting an empty container??
            // This error of empty container not being valid may not even be an issue once we move logic for resources into the specific slots
            current = stack.copy();
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
            return stack;
        }
        int needed = getLimit(stack) - current.getCount();
        if (needed <= 0) {
            //Fail if we are a full slot
            return stack;
        }
        boolean sameType = false;
        if (current.isEmpty() || (sameType = ItemHandlerHelper.canItemStacksStack(current, stack))) {
            int toAdd = Math.min(stack.getCount(), needed);
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
            return StackUtils.size(stack, stack.getCount() - toAdd);
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

    //TODO: Evaluate usages of this maybe some should be capped by the max size of the stack
    // In fact most uses of this probably can instead use the insertItem method instead
    @Override
    public int getLimit(@NonNull ItemStack stack) {
        //TODO: is this a decent way to do this or do we want to set obeyStack limit some other way
        return obeyStackLimit && !stack.isEmpty() ? Math.min(limit, stack.getMaxStackSize()) : limit;
    }

    @Override
    public boolean isItemValid(@Nonnull ItemStack stack) {
        return validator.test(stack);
    }

    @Override
    public void onContentsChanged() {
        if (inventory != null) {
            inventory.onContentsChanged();
        }
    }

    //TODO: Should we move InventoryContainerSlot to the API and reference that instead
    @Nullable
    @Override
    public InventoryContainerSlot createContainerSlot() {
        return new InventoryContainerSlot(this, x, y, getSlotType());
    }

    //TODO: Implement this properly in the different subclasses/slot types
    protected ContainerSlotType getSlotType() {
        return ContainerSlotType.NORMAL;
    }

    /**
     * {@inheritDoc}
     *
     * @implNote Overwritten as we return a cached/copy of our stack in {@link #getStack()}, and we can optimize out the copying, and can also directly modify our stack
     * instead of having to make a copy.
     */
    @Override
    public int setStackSize(int amount, Action action) {
        if (current.isEmpty()) {
            return 0;
        }
        if (amount <= 0) {
            if (action.execute()) {
                setStack(ItemStack.EMPTY);
            }
            return 0;
        }
        int maxStackSize = getLimit(current);
        if (amount > maxStackSize) {
            amount = maxStackSize;
        }
        if (current.getCount() == amount || action.simulate()) {
            //If our size is not changing or we are only simulating the change, don't do anything
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
    public int growStack(int amount, Action action) {
        int currentCount = current.getCount();
        int newSize = setStackSize(currentCount + amount, action);
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

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        if (!current.isEmpty()) {
            nbt.put("Item", current.write(new CompoundNBT()));
            if (current.getCount() > current.getMaxStackSize()) {
                nbt.putInt("SizeOverride", current.getCount());
            }
        }
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        if (nbt.contains("Item", NBT.TAG_COMPOUND)) {
            ItemStack stack = ItemStack.read(nbt.getCompound("Item"));
            if (nbt.contains("SizeOverride", NBT.TAG_INT)) {
                stack.setCount(nbt.getInt("SizeOverride"));
            }
            //Directly set the stack in case the item is no longer valid for the stack.
            //TODO: Re-evaluate as this may cause issues but we really don't want to just void the stack and then throw an exception
            // Should we at least log a warning that it is no longer valid if it isn't valid?
            current = stack;
        } else {
            current = ItemStack.EMPTY;
        }
        //TODO: Do we need to fire onContentsChanged??? Probably not given this is mainly used for when we just loaded from disk anyways
    }
}