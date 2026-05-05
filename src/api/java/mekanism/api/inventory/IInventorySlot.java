package mekanism.api.inventory;

import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.SerializationConstants;
import mekanism.api.SerializerHelper;
import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

//TODO - 26.1: make this implement/supply ItemAccess/ResourceHandler? It currently has a pseudo ItemHandler impl, so might be better to move everything away from single-slot context?
@NothingNullByDefault
public interface IInventorySlot extends ValueIOSerializable, IContentsListener {

    /**
     * Returns the {@link ItemStack} in this {@link IInventorySlot}.
     * <p>
     * The result's stack size may be greater than the itemstack's max size.
     * <p>
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
    default ItemStack getStack() {
        return getResource().toStack(getCount());
    }

    //TODO - 26.1: Docs and replace getStack/etc with this and getAmount
    ItemResource getResource();

    /**
     * Overrides the stack in this {@link IInventorySlot}.
     *
     * @param stack {@link ItemStack} to set this slot to (may be empty).
     *
     * @throws RuntimeException if this slot is called in a way that it was not expecting.
     * @implNote If the internal stack does get updated make sure to call {@link #onContentsChanged()}
     */
    @Deprecated(forRemoval = true)//TODO - 26.1: Move calls to setStack(ItemResource, int)
    default void setStack(ItemStack stack) {
        setStack(ItemResource.of(stack), stack.count());
    }

    //TODO - 26.1: Docs, and transition calls to setStack(ItemStack) to this
    void setStack(ItemResource itemType, int storedAmount);

    //TODO - 26.1: Docs
    int insert(ItemResource resource, int amount, TransactionContext transaction, AutomationType automationType);

    //TODO - 26.1: Docs
    //TODO - 26.1: Check callers and make sure none are relying on the fact that in the past it would return at most max stack size
    int extract(ItemResource resource, int amount, TransactionContext transaction, AutomationType automationType);

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
    int getLimit(ItemResource resource);//TODO - 26.1: Update docs

    //TODO - 26.1: Re-evaluate name and add docs
    //TODO - 26.1: Should bin slots override this to check lock stack? Probably
    default int getCurrentLimit() {
        return getLimit(getResource());
    }

    /**
     * <p>
     * This function re-implements the vanilla function {@link net.minecraft.world.Container#canPlaceItem(int, ItemStack)}. It should be used instead of simulated
     * insertions in cases where the contents and state of the inventory are irrelevant, mainly for the purpose of automation and logic (for instance, testing if a
     * minecart can wait to deposit its items into a full inventory, or if the items in the minecart can never be placed into the inventory and should move on).
     * </p>
     * <ul>
     * <li>isItemValid is false when insertion of the item is never valid.</li>
     * <li>When isItemValid is true, no assumptions can be made and insertion must be simulated case-by-case.</li>
     * <li>The actual items in the inventory, its fullness, or any other state are <strong>not</strong> considered by isItemValid.</li>
     * </ul>
     *
     * @param stack Stack to test with for validity
     *
     * @return true if this {@link IInventorySlot} can accept the {@link ItemStack}, not considering the current state of the inventory. false if this
     * {@link IInventorySlot} can never insert the {@link ItemStack} in any situation.
     */
    boolean isValid(ItemResource itemType);//TODO - 26.1: Update docs and figure out handling of empty resource

    /**
     * Returns a slot for use in auto adding slots to a container.
     *
     * @return A slot for use in a container that represents this {@link IInventorySlot}, or null if this slot should not be added.
     */
    @Nullable
    default Slot createContainerSlot() {
        return null;
    }

    /**
     * Convenience method for modifying the size of the stored stack.
     * <p>
     * If there is a stack stored in this slot, set the size of it to the given amount. Capping at the item's max stack size and the limit of this slot. If the amount is
     * less than or equal to zero, then this instead sets the stack to the empty stack.
     *
     * @param amount The desired size to set the stack to.
     * @param action The action to perform, either {@link Action#EXECUTE} or {@link Action#SIMULATE}
     *
     * @return Actual size the stack was set to.
     */
    default int setStackSize(int amount, Action action) {//TODO - 26.1: Make this, shrinkStack, and growStack be transactional
        if (isEmpty()) {
            return 0;
        } else if (amount <= 0) {
            if (action.execute()) {
                setEmpty();
            }
            return 0;
        }
        int maxStackSize = getCurrentLimit();
        if (amount > maxStackSize) {
            amount = maxStackSize;
        }
        if (getCount() == amount || action.simulate()) {
            //If our size is not changing, or we are only simulating the change, don't do anything
            return amount;
        }
        setStack(getResource(), amount);
        return amount;
    }

    /**
     * Convenience method for growing the size of the stored stack.
     * <p>
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
        if (current == 0) {
            //"Fail quick" if our stack is empty, so we can't grow it
            return 0;
        } else if (amount > 0) {
            //Cap adding amount at how much we need, so that we don't risk integer overflow
            amount = Math.min(amount, getCurrentLimit());
        }
        int newSize = setStackSize(current + amount, action);
        return newSize - current;
    }

    /**
     * Convenience method for shrinking the size of the stored stack.
     * <p>
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
     */
    default boolean isEmpty() {//TODO - 26.1: Should we also validate that the amount isn't somehow zero?
        return getResource().isEmpty();
    }

    /**
     * Convenience method for emptying this {@link IInventorySlot}.
     */
    default void setEmpty() {
        setStack(ItemResource.EMPTY, 0);
    }

    /**
     * Convenience method for checking the size of the stack in this slot.
     *
     * @return The size of the stored stack, or zero is the stack is empty.
     */
    int getCount();
    //TODO - 26.1: Do we want to have two forms of get amount for our slot type similar to how the handler supports reporting a long variant?
    // Also do we want to rename this to getAmount if we potentially make a generic super interface between inventory slots and other resource types?

    @Override
    default void serialize(ValueOutput output) {
        if (!isEmpty()) {
            //TODO - 26.1: Reimplement this to save the resource and amount rather than having it as an oversized stack
            output.store(SerializationConstants.ITEM, SerializerHelper.OVERSIZED_ITEM_CODEC, getStack());
        }
    }
}