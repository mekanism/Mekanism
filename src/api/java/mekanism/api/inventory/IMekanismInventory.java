package mekanism.api.inventory;

import java.util.List;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public interface IMekanismInventory extends IItemHandlerModifiable, IContentsListener {

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
     * @since 10.8.0
     */
    List<IInventorySlot> getInventorySlots();

    /**
     * Returns the {@link IInventorySlot} that has the given index from the list of slots on the given side.
     *
     * @param slot The index of the slot to retrieve.
     * @param side The side we are interacting with the handler from (null for internal).
     *
     * @return The {@link IInventorySlot} that has the given index from the list of slots on the given side.
     * @since 10.8.0
     */
    @Nullable
    default IInventorySlot getInventorySlot(int slot) {
        List<IInventorySlot> slots = getInventorySlots();
        return slot >= 0 && slot < slots.size() ? slots.get(slot) : null;
    }

    @Override
    default void setStackInSlot(int slot, ItemStack stack) {
        IInventorySlot inventorySlot = getInventorySlot(slot);
        if (inventorySlot != null) {
            inventorySlot.setStack(stack);
        }
    }

    @Override
    default int getSlots() {
        return getInventorySlots().size();
    }

    @Override
    default ItemStack getStackInSlot(int slot) {
        IInventorySlot inventorySlot = getInventorySlot(slot);
        return inventorySlot == null ? ItemStack.EMPTY : inventorySlot.getStack();
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
     * @since 10.8.0
     */
    default ItemStack insertItem(int slot, ItemStack stack, Action action, AutomationType automationType) {
        IInventorySlot inventorySlot = getInventorySlot(slot);
        if (inventorySlot == null) {
            return stack;
        }
        return inventorySlot.insertItem(stack, action, automationType);
    }

    @Override
    @ApiStatus.NonExtendable
    default ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {//TODO - 26.1: Re-evaluate, previously was in ISidedItemHandler
        return insertItem(slot, stack, Action.get(!simulate), AutomationType.INTERNAL);
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
     * @since 10.8.0
     */
    default ItemStack extractItem(int slot, int amount, Action action, AutomationType automationType) {
        IInventorySlot inventorySlot = getInventorySlot(slot);
        if (inventorySlot == null) {
            return ItemStack.EMPTY;
        }
        return inventorySlot.extractItem(amount, action, automationType);
    }

    @Override
    @ApiStatus.NonExtendable
    default ItemStack extractItem(int slot, int amount, boolean simulate) {//TODO - 26.1: Re-evaluate, previously was in ISidedItemHandler
        return extractItem(slot, amount, Action.get(!simulate), AutomationType.INTERNAL);
    }

    @Override
    default int getSlotLimit(int slot) {
        IInventorySlot inventorySlot = getInventorySlot(slot);
        return inventorySlot == null ? 0 : inventorySlot.getLimit(ItemStack.EMPTY);
    }

    @Override
    default boolean isItemValid(int slot, ItemStack stack) {
        IInventorySlot inventorySlot = getInventorySlot(slot);
        return inventorySlot != null && inventorySlot.isItemValid(stack);
    }

    /**
     * Sided inventory helper for isEmpty
     *
     * @return true if completely empty on the default side
     *
     * @since 10.4.0
     */
    default boolean isInventoryEmpty() {
        for (IInventorySlot slot : getInventorySlots()) {
            if (!slot.isEmpty()) {
                return false;
            }
        }
        return true;
    }
}