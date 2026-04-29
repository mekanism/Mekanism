package mekanism.api.inventory;

import java.util.List;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public interface IMekanismInventory extends IItemHandlerModifiable, IContentsListener {

    /**
     * The side this {@link IMekanismInventory} is for. This defaults to null, which is for internal use.
     *
     * @return The default side to use for the normal {@link IItemHandler} methods when wrapping them into {@link IMekanismInventory} methods.
     */
    @Nullable
    default Direction getInventorySideFor() {//TODO - 26.1: Re-evaluate, previously was in ISidedItemHandler
        return null;
    }

    /**
     * Used to check if an instance of {@link IMekanismInventory} actually has an inventory.
     *
     * @return True if we are actually an inventory.
     *
     * @apiNote If for some reason you are comparing to {@link IMekanismInventory} without having gotten the object via the item handler capability, then you must call
     * this method to make sure that it really is an inventory. As most mekanism tiles have this class in their hierarchy.
     * @implNote If this returns false the capability should not be exposed AND methods should turn reasonable defaults for not doing anything.
     */
    default boolean hasInventory() {
        return true;
    }

    /**
     * Returns the list of IInventorySlots that this inventory exposes on the given side.
     *
     * @param side The side we are interacting with the handler from (null for internal).
     *
     * @return The list of all IInventorySlots that this {@link IMekanismInventory} contains for the given side. If there are no slots for the side or
     * {@link #hasInventory()} is false then it returns an empty list.
     *
     * @implNote When side is null (an internal request), this method <em>MUST</em> return all slots in the inventory. This will be used by the container generating code
     * to add all the proper slots that are needed. Additionally, if {@link #hasInventory()} is false, this <em>MUST</em> return an empty list.
     */
    List<IInventorySlot> getInventorySlots(@Nullable Direction side);

    /**
     * Returns the {@link IInventorySlot} that has the given index from the list of slots on the given side.
     *
     * @param slot The index of the slot to retrieve.
     * @param side The side we are interacting with the handler from (null for internal).
     *
     * @return The {@link IInventorySlot} that has the given index from the list of slots on the given side.
     */
    @Nullable
    default IInventorySlot getInventorySlot(int slot, @Nullable Direction side) {
        List<IInventorySlot> slots = getInventorySlots(side);
        return slot >= 0 && slot < slots.size() ? slots.get(slot) : null;
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
    default void setStackInSlot(int slot, ItemStack stack, @Nullable Direction side) {
        IInventorySlot inventorySlot = getInventorySlot(slot, side);
        if (inventorySlot != null) {
            inventorySlot.setStack(stack);
        }
    }

    @Override
    default void setStackInSlot(int slot, ItemStack stack) {//TODO - 26.1: Re-evaluate, previously was in ISidedItemHandler
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
    default int getSlots(@Nullable Direction side) {
        return getInventorySlots(side).size();
    }

    @Override
    default int getSlots() {//TODO - 26.1: Re-evaluate, previously was in ISidedItemHandler
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
    default ItemStack getStackInSlot(int slot, @Nullable Direction side) {
        IInventorySlot inventorySlot = getInventorySlot(slot, side);
        return inventorySlot == null ? ItemStack.EMPTY : inventorySlot.getStack();
    }

    @Override
    default ItemStack getStackInSlot(int slot) {//TODO - 26.1: Re-evaluate, previously was in ISidedItemHandler
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
    default ItemStack insertItem(int slot, ItemStack stack, @Nullable Direction side, Action action) {
        IInventorySlot inventorySlot = getInventorySlot(slot, side);
        if (inventorySlot == null) {
            return stack;
        }
        return inventorySlot.insertItem(stack, action, AutomationType.handler(side));
    }

    @Override
    default ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {//TODO - 26.1: Re-evaluate, previously was in ISidedItemHandler
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
    default ItemStack extractItem(int slot, int amount, @Nullable Direction side, Action action) {
        IInventorySlot inventorySlot = getInventorySlot(slot, side);
        if (inventorySlot == null) {
            return ItemStack.EMPTY;
        }
        return inventorySlot.extractItem(amount, action, AutomationType.handler(side));
    }

    @Override
    default ItemStack extractItem(int slot, int amount, boolean simulate) {//TODO - 26.1: Re-evaluate, previously was in ISidedItemHandler
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
    default int getSlotLimit(int slot, @Nullable Direction side) {
        IInventorySlot inventorySlot = getInventorySlot(slot, side);
        return inventorySlot == null ? 0 : inventorySlot.getLimit(ItemStack.EMPTY);
    }

    @Override
    default int getSlotLimit(int slot) {//TODO - 26.1: Re-evaluate, previously was in ISidedItemHandler
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
    default boolean isItemValid(int slot, ItemStack stack, @Nullable Direction side) {
        IInventorySlot inventorySlot = getInventorySlot(slot, side);
        return inventorySlot != null && inventorySlot.isItemValid(stack);
    }

    @Override
    default boolean isItemValid(int slot, ItemStack stack) {//TODO - 26.1: Re-evaluate, previously was in ISidedItemHandler
        return isItemValid(slot, stack, getInventorySideFor());
    }

    /**
     * Are all the Slots empty?
     *
     * @param side the side to query
     *
     * @return true if completely empty on this side
     *
     * @implNote named isInventoryEmpty to avoid clashing with any other isEmpty() method
     * @since 10.4.0
     */
    default boolean isInventoryEmpty(@Nullable Direction side) {
        for (IInventorySlot slot : getInventorySlots(side)) {
            if (!slot.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Sided inventory helper for isEmpty
     *
     * @return true if completely empty on the default side
     *
     * @since 10.4.0
     */
    default boolean isInventoryEmpty() {
        return isInventoryEmpty(getInventorySideFor());
    }
}