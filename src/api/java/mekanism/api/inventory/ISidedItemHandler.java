package mekanism.api.inventory;

import mekanism.api.Action;
import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.Nullable;

/**
 * A sided variant of {@link IItemHandlerModifiable}
 */
@NothingNullByDefault
public interface ISidedItemHandler extends IItemHandlerModifiable {

    /**
     * The side this {@link ISidedItemHandler} is for. This defaults to null, which is for internal use.
     *
     * @return The default side to use for the normal {@link IItemHandler} methods when wrapping them into {@link ISidedItemHandler} methods.
     */
    @Nullable
    default Direction getInventorySideFor() {
        return null;
    }

    /**
     * A sided variant of {@link IItemHandlerModifiable#setStackInSlot(int, ItemStack)}, docs copied for convenience.
     * <p>
     * Overrides the stack in the given slot. This method is used by the standard Forge helper methods and classes. It is not intended for general use by other mods, and
     * the handler may throw an error if it is called unexpectedly.
     *
     * @param slot  Slot to modify
     * @param stack {@link ItemStack} to set slot to (may be empty).
     * @param side  The side we are interacting with the handler from (null for internal).
     *
     * @throws RuntimeException if the handler is called in a way that the handler was not expecting.
     */
    void setStackInSlot(int slot, ItemStack stack, @Nullable Direction side);

    @Override
    default void setStackInSlot(int slot, ItemStack stack) {
        setStackInSlot(slot, stack, getInventorySideFor());
    }

    /**
     * A sided variant of {@link IItemHandler#getSlots()}, docs copied for convenience.
     * <p>
     * Returns the number of slots available
     *
     * @param side The side we are interacting with the handler from (null for internal).
     *
     * @return The number of slots available
     */
    int getSlots(@Nullable Direction side);

    @Override
    default int getSlots() {
        return getSlots(getInventorySideFor());
    }

    /**
     * A sided variant of {@link IItemHandler#getStackInSlot(int)}, docs copied for convenience.
     * <p>
     * Returns the {@link ItemStack} in a given slot.
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
     * @param slot Slot to query
     * @param side The side we are interacting with the handler from (null for internal).
     *
     * @return {@link ItemStack} in given slot. Empty {@link ItemStack} if the slot is empty.
     *
     * @apiNote <strong>IMPORTANT:</strong> Do not modify this {@link ItemStack}.
     */
    ItemStack getStackInSlot(int slot, @Nullable Direction side);

    @Override
    default ItemStack getStackInSlot(int slot) {
        return getStackInSlot(slot, getInventorySideFor());
    }

    /**
     * A sided variant of {@link IItemHandler#insertItem(int, ItemStack, boolean)}, docs copied for convenience.
     *
     * <p>
     * Inserts an {@link ItemStack} into the given slot and return the remainder. The {@link ItemStack} <em>should not</em> be modified in this function!
     * </p>
     * Note: This behaviour is subtly different from {@link IFluidHandler#fill(FluidStack, IFluidHandler.FluidAction)}
     *
     * @param slot   Slot to insert into.
     * @param stack  {@link ItemStack} to insert. This must not be modified by the item handler.
     * @param side   The side we are interacting with the handler from (null for internal).
     * @param action The action to perform, either {@link Action#EXECUTE} or {@link Action#SIMULATE}
     *
     * @return The remaining {@link ItemStack} that was not inserted (if the entire stack is accepted, then return an empty {@link ItemStack}). May be the same as the
     * input {@link ItemStack} if unchanged, otherwise a new {@link ItemStack}. The returned ItemStack can be safely modified after
     *
     * @implNote The {@link ItemStack} <em>should not</em> be modified in this function!
     */
    ItemStack insertItem(int slot, ItemStack stack, @Nullable Direction side, Action action);

    @Override
    default ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        return insertItem(slot, stack, getInventorySideFor(), Action.get(!simulate));
    }

    /**
     * A sided variant of {@link IItemHandler#extractItem(int, int, boolean)}, docs copied for convenience.
     * <p>
     * Extracts an {@link ItemStack} from the given slot.
     * <p>
     * The returned value must be empty if nothing is extracted, otherwise its stack size must be less than or equal to {@code amount} and
     * {@link ItemStack#getMaxStackSize()}.
     * </p>
     *
     * @param slot   Slot to extract from.
     * @param amount Amount to extract (may be greater than the current stack's max limit)
     * @param side   The side we are interacting with the handler from (null for internal).
     * @param action The action to perform, either {@link Action#EXECUTE} or {@link Action#SIMULATE}
     *
     * @return {@link ItemStack} extracted from the slot, must be empty if nothing can be extracted. The returned {@link ItemStack} can be safely modified after, so item
     * handlers should return a new or copied stack.
     *
     * @implNote The returned {@link ItemStack} can be safely modified after, so a new or copied stack should be returned.
     */
    ItemStack extractItem(int slot, int amount, @Nullable Direction side, Action action);

    @Override
    default ItemStack extractItem(int slot, int amount, boolean simulate) {
        return extractItem(slot, amount, getInventorySideFor(), Action.get(!simulate));
    }

    /**
     * A sided variant of {@link IItemHandler#getSlotLimit(int)}, docs copied for convenience.
     * <p>
     * Retrieves the maximum stack size allowed to exist in the given slot.
     *
     * @param slot Slot to query.
     * @param side The side we are interacting with the handler from (null for internal).
     *
     * @return The maximum stack size allowed in the slot.
     */
    int getSlotLimit(int slot, @Nullable Direction side);

    @Override
    default int getSlotLimit(int slot) {
        return getSlotLimit(slot, getInventorySideFor());
    }

    /**
     * A sided variant of {@link IItemHandler#isItemValid(int, ItemStack)}, docs copied for convenience.
     *
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
     * @param slot  Slot to query for validity
     * @param stack Stack to test with for validity
     * @param side  The side we are interacting with the handler from (null for internal).
     *
     * @return true if the slot can accept the {@link ItemStack}, not considering the current state of the inventory. false if the slot can never insert the
     * {@link ItemStack} in any situation.
     */
    boolean isItemValid(int slot, ItemStack stack, @Nullable Direction side);

    @Override
    default boolean isItemValid(int slot, ItemStack stack) {
        return isItemValid(slot, stack, getInventorySideFor());
    }
}