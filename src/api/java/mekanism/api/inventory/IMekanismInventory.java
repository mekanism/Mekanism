package mekanism.api.inventory;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Action;
import mekanism.api.inventory.slot.IInventorySlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;

//TODO: Go through all the usages of ItemStack setCount, grow, and shrink as any that are from inventories should have the slot change it,
// rather than forcefully changing it when the API says it shouldn't be modified
// ALSO make sure to check all getStackInSlot calls to see if they are then being passed to stuff that may modify them
public interface IMekanismInventory extends ISidedItemHandler {

    /**
     * Used to check if an instance of IMekanismInventory actually has an inventory.
     *
     * @return True if we are actually an inventory.
     *
     * @apiNote If for some reason you are comparing to IMekanismInventory without having gotten the object via the item handler capability, then you must call this
     * method to make sure that it really is an inventory. As most mekanism tiles have this class in their hierarchy.
     * @implNote If this returns false the capability should not be exposed AND methods should turn reasonable defaults for not doing anything.
     */
    default boolean hasInventory() {
        return true;
    }

    //TODO: Use this for generating the container code. Should even be able to make the shift click code be based off of the logic in the individual inventory slots
    // Note: for generating container code this should pass null as the side to get all the information
    //TODO: We should have our inventories cache the list of slots for the different sides, as this method may be called a decent amount for things such as getSlots
    //TODO: Would it make sense to inline some calculations for mekanism inventories rather then getting the specific slot in an index

    /**
     * Returns the list of IInventorySlots that this inventory exposes on the given side.
     *
     * @param side The side we are interacting with the handler from (null for internal).
     *
     * @return The list of all IInventorySlots that this {@link IMekanismInventory} contains for the given side. If there are no slots for the side or {@link
     * #hasInventory()} is false then it returns an empty list.
     *
     * @implNote When side is null (an internal request), this method <em>MUST</em> return all slots in the inventory. This will be used by the container generating code
     * to add all the proper slots that are needed. Additionally, if {@link #hasInventory()} is false, this <em>MUST</em> return an empty list.
     */
    @Nonnull
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
        if (slot < slots.size()) {
            return slots.get(slot);
        }
        return null;
    }

    @Override
    default void setStackInSlot(int slot, @Nonnull ItemStack stack, @Nullable Direction side) {
        IInventorySlot inventorySlot = getInventorySlot(slot, side);
        if (inventorySlot != null) {
            inventorySlot.setStack(stack);
        }
    }

    @Override
    default int getSlots(@Nullable Direction side) {
        return getInventorySlots(side).size();
    }

    @Nonnull
    @Override
    default ItemStack getStackInSlot(int slot, @Nullable Direction side) {
        IInventorySlot inventorySlot = getInventorySlot(slot, side);
        return inventorySlot == null ? ItemStack.EMPTY : inventorySlot.getStack();
    }

    @Nonnull
    @Override
    default ItemStack insertItem(int slot, @Nonnull ItemStack stack, @Nullable Direction side, Action action) {
        IInventorySlot inventorySlot = getInventorySlot(slot, side);
        if (inventorySlot == null) {
            return stack;
        }
        return inventorySlot.insertItem(stack, action);
    }

    @Nonnull
    @Override
    default ItemStack extractItem(int slot, int amount, @Nullable Direction side, Action action) {
        IInventorySlot inventorySlot = getInventorySlot(slot, side);
        if (inventorySlot == null) {
            return ItemStack.EMPTY;
        }
        return inventorySlot.extractItem(amount, action);
    }

    @Override
    default int getSlotLimit(int slot, @Nullable Direction side) {
        IInventorySlot inventorySlot = getInventorySlot(slot, side);
        return inventorySlot == null ? 0 : inventorySlot.getLimit();
    }

    @Override
    default boolean isItemValid(int slot, @Nonnull ItemStack stack, @Nullable Direction side) {
        IInventorySlot inventorySlot = getInventorySlot(slot, side);
        return inventorySlot != null && inventorySlot.isItemValid(stack);
    }
}