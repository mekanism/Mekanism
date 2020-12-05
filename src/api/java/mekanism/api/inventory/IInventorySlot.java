package mekanism.api.inventory;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.Action;
import mekanism.api.IContentsListener;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IInventorySlot extends INBTSerializable<CompoundNBT>, IContentsListener {

    /**
     * Returns the {@link ItemStack} in this {@link IInventorySlot}.
     *
     * The result's stack size may be greater than the itemstack's max size.
     *
     * If the result is empty, then the slot is empty.
     *
     * <p>
     * <strong>IMPORTANT:</strong> This {@link ItemStack} <em>MUST NOT</em> be modified. This method is not for altering an inventory's contents. Any implementers who
     * are able to detect modification through this method should throw an exception.
     * </p>
     * <p>
     * <strong><em>SERIOUSLY: DO NOT MODIFY THE RETURNED ITEMSTACK</em></strong>
     * </p>
     *
     * @return {@link ItemStack} in this {@link IInventorySlot}. Empty {@link ItemStack} if this {@link IInventorySlot} is empty.
     *
     * @apiNote <strong>IMPORTANT:</strong> Do not modify this {@link ItemStack}.
     */
    ItemStack getStack();

    /**
     * Overrides the stack in this {@link IInventorySlot}.
     *
     * @param stack {@link ItemStack} to set this slot to (may be empty).
     *
     * @throws RuntimeException if this slot is called in a way that it was not expecting.
     * @implNote If the internal stack does get updated make sure to call {@link #onContentsChanged()}
     */
    void setStack(ItemStack stack);

    /**
     * <p>
     * Inserts an {@link ItemStack} into this {@link IInventorySlot} and return the remainder. The {@link ItemStack} <em>should not</em> be modified in this function!
     * </p>
     * Note: This behaviour is subtly different from {@link net.minecraftforge.fluids.capability.IFluidHandler#fill(net.minecraftforge.fluids.FluidStack,
     * net.minecraftforge.fluids.capability.IFluidHandler.FluidAction)}
     *
     * @param stack          {@link ItemStack} to insert. This must not be modified by the slot.
     * @param action         The action to perform, either {@link Action#EXECUTE} or {@link Action#SIMULATE}
     * @param automationType The method that this slot is being interacted from.
     *
     * @return The remaining {@link ItemStack} that was not inserted (if the entire stack is accepted, then return an empty {@link ItemStack}). May be the same as the
     * input {@link ItemStack} if unchanged, otherwise a new {@link ItemStack}. The returned ItemStack can be safely modified after
     *
     * @implNote The {@link ItemStack} <em>should not</em> be modified in this function! If the internal stack does get updated make sure to call {@link
     * #onContentsChanged()}. It is also recommended to override this if your internal {@link ItemStack} is mutable so that a copy does not have to be made every run
     */
    default ItemStack insertItem(ItemStack stack, Action action, AutomationType automationType) {
        if (stack.isEmpty() || !isItemValid(stack)) {
            //"Fail quick" if the given stack is empty or we can never insert the item or currently are unable to insert it
            return stack;
        }
        int needed = getLimit(stack) - getCount();
        if (needed <= 0) {
            //Fail if we are a full slot
            return stack;
        }
        boolean sameType = false;
        if (isEmpty() || (sameType = ItemHandlerHelper.canItemStacksStack(getStack(), stack))) {
            int toAdd = Math.min(stack.getCount(), needed);
            if (action.execute()) {
                //If we want to actually insert the item, then update the current item
                if (sameType) {
                    // Note: this also will mark that the contents changed
                    //We can just grow our stack by the amount we want to increase it
                    growStack(toAdd, action);
                } else {
                    //If we are not the same type then we have to copy the stack and set it
                    // Note: this also will mark that the contents changed
                    ItemStack toSet = stack.copy();
                    toSet.setCount(toAdd);
                    setStack(toSet);
                }
            }
            ItemStack remainder = stack.copy();
            remainder.setCount(stack.getCount() - toAdd);
            return remainder;
        }
        //If we didn't accept this item, then just return the given stack
        return stack;
    }

    /**
     * Extracts an {@link ItemStack} from this {@link IInventorySlot}.
     * <p>
     * The returned value must be empty if nothing is extracted, otherwise its stack size must be less than or equal to {@code amount} and {@link
     * ItemStack#getMaxStackSize()}.
     * </p>
     *
     * @param amount         Amount to extract (may be greater than the current stack's max limit)
     * @param action         The action to perform, either {@link Action#EXECUTE} or {@link Action#SIMULATE}
     * @param automationType The method that this slot is being interacted from.
     *
     * @return {@link ItemStack} extracted from the slot, must be empty if nothing can be extracted. The returned {@link ItemStack} can be safely modified after, so the
     * slot should return a new or copied stack.
     *
     * @implNote The returned {@link ItemStack} can be safely modified after, so a new or copied stack should be returned. If the internal stack does get updated make
     * sure to call {@link #onContentsChanged()}. It is also recommended to override this if your internal {@link ItemStack} is mutable so that a copy does not have to be
     * made every run
     */
    default ItemStack extractItem(int amount, Action action, AutomationType automationType) {
        if (isEmpty() || amount < 1) {
            //"Fail quick" if we don't can never extract from this slot, have an item stored, or the amount being requested is less than one
            return ItemStack.EMPTY;
        }
        ItemStack current = getStack();
        //Ensure that if this slot allows going past the max stack size of an item, that when extracting we don't act as if we have more than
        // the max stack size, as the JavaDoc for IItemHandler requires that the returned stack is not larger than its stack size
        int currentAmount = Math.min(getCount(), current.getMaxStackSize());
        if (currentAmount < amount) {
            //If we are trying to extract more than we have, just change it so that we are extracting it all
            amount = currentAmount;
        }
        //Note: While we technically could just return the stack itself if we are removing all that we have, it would require a lot more checks
        // especially for supporting the fact of limiting by the max stack size.
        ItemStack toReturn = current.copy();
        toReturn.setCount(amount);
        if (action.execute()) {
            //If shrink gets the size to zero it will update the empty state so that isEmpty() returns true.
            // Note: this also will mark that the contents changed
            shrinkStack(amount, action);
        }
        return toReturn;
    }

    /**
     * Retrieves the maximum stack size allowed to exist in this {@link IInventorySlot}. Unlike {@link IItemHandler#getSlotLimit(int)} this takes a stack that it can use
     * for checking max stack size, if this {@link IInventorySlot} wants to respect the maximum stack size.
     *
     * @param stack The stack we want to know the limit for in case this {@link IInventorySlot} wants to obey the stack limit. If the empty stack is passed, then it
     *              returns the max amount of any item this slot can store.
     *
     * @return The maximum stack size allowed in this {@link IInventorySlot}.
     *
     * @implNote The implementation of this CAN take into account the max size of this stack but is not required to.
     */
    int getLimit(ItemStack stack);

    /**
     * <p>
     * This function re-implements the vanilla function {@link IInventory#isItemValidForSlot(int, ItemStack)}. It should be used instead of simulated insertions in cases
     * where the contents and state of the inventory are irrelevant, mainly for the purpose of automation and logic (for instance, testing if a minecart can wait to
     * deposit its items into a full inventory, or if the items in the minecart can never be placed into the inventory and should move on).
     * </p>
     * <ul>
     * <li>isItemValid is false when insertion of the item is never valid.</li>
     * <li>When isItemValid is true, no assumptions can be made and insertion must be simulated case-by-case.</li>
     * <li>The actual items in the inventory, its fullness, or any other state are <strong>not</strong> considered by isItemValid.</li>
     * </ul>
     *
     * @param stack Stack to test with for validity
     *
     * @return true if this {@link IInventorySlot} can accept the {@link ItemStack}, not considering the current state of the inventory. false if this {@link
     * IInventorySlot} can never insert the {@link ItemStack} in any situation.
     */
    boolean isItemValid(ItemStack stack);

    /**
     * Returns a slot for use in auto adding slots to a container.
     *
     * @return A slot for use in a container that represents this {@link IInventorySlot}, or null if this slot should not be added.
     */
    @Nullable
    Slot createContainerSlot();

    /**
     * Convenience method for modifying the size of the stored stack.
     *
     * If there is a stack stored in this slot, set the size of it to the given amount. Capping at the item's max stack size and the limit of this slot. If the amount is
     * less than or equal to zero, then this instead sets the stack to the empty stack.
     *
     * @param amount The desired size to set the stack to.
     * @param action The action to perform, either {@link Action#EXECUTE} or {@link Action#SIMULATE}
     *
     * @return Actual size the stack was set to.
     *
     * @implNote It is recommended to override this if your internal {@link ItemStack} is mutable so that a copy does not have to be made every run. If the internal stack
     * does get updated make sure to call {@link #onContentsChanged()}
     */
    default int setStackSize(int amount, Action action) {
        if (isEmpty()) {
            return 0;
        } else if (amount <= 0) {
            if (action.execute()) {
                setEmpty();
            }
            return 0;
        }
        ItemStack stack = getStack();
        int maxStackSize = getLimit(stack);
        if (amount > maxStackSize) {
            amount = maxStackSize;
        }
        if (stack.getCount() == amount || action.simulate()) {
            //If our size is not changing or we are only simulating the change, don't do anything
            return amount;
        }
        ItemStack newStack = stack.copy();
        newStack.setCount(amount);
        setStack(newStack);
        return amount;
    }

    /**
     * Convenience method for growing the size of the stored stack.
     *
     * If there is a stack stored in this slot, increase its size by the given amount. Capping at the item's max stack size and the limit of this slot. If the stack
     * shrinks to an amount of less than or equal to zero, then this instead sets the stack to the empty stack.
     *
     * @param amount The desired amount to grow the stack by.
     * @param action The action to perform, either {@link Action#EXECUTE} or {@link Action#SIMULATE}
     *
     * @return Actual amount the stack grew.
     *
     * @apiNote Negative values for amount are valid, and will instead cause the stack to shrink.
     * @implNote If the internal stack does get updated make sure to call {@link #onContentsChanged()}
     */
    default int growStack(int amount, Action action) {
        int current = getCount();
        if (amount > 0) {
            //Cap adding amount at how much we need, so that we don't risk integer overflow
            amount = Math.min(amount, getLimit(getStack()));
        }
        int newSize = setStackSize(current + amount, action);
        return newSize - current;
    }

    /**
     * Convenience method for shrinking the size of the stored stack.
     *
     * If there is a stack stored in this slot, shrink its size by the given amount. If this causes its size to become less than or equal to zero, then the stack is set
     * to the empty stack. If this method is used to grow the stack the size gets capped at the item's max stack size and the limit of this slot.
     *
     * @param amount The desired amount to shrink the stack by.
     * @param action The action to perform, either {@link Action#EXECUTE} or {@link Action#SIMULATE}
     *
     * @return Actual amount the stack shrunk.
     *
     * @apiNote Negative values for amount are valid, and will instead cause the stack to grow.
     * @implNote If the internal stack does get updated make sure to call {@link #onContentsChanged()}
     */
    default int shrinkStack(int amount, Action action) {
        return -growStack(-amount, action);
    }

    /**
     * Convenience method for checking if this slot is empty.
     *
     * @return True if the slot is empty, false otherwise.
     *
     * @implNote If your implementation of {@link #getStack()} returns a copy, this should be overridden to directly check against the internal stack.
     */
    default boolean isEmpty() {
        return getStack().isEmpty();
    }

    /**
     * Convenience method for emptying this {@link IInventorySlot}.
     */
    default void setEmpty() {
        setStack(ItemStack.EMPTY);
    }

    /**
     * Convenience method for checking the size of the stack in this slot.
     *
     * @return The size of the stored stack, or zero is the stack is empty.
     *
     * @implNote If your implementation of {@link #getStack()} returns a copy, this should be overridden to directly check against the internal stack.
     */
    default int getCount() {
        return getStack().getCount();
    }
}