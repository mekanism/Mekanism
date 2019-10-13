package mekanism.common.inventory.container.slot;

import javax.annotation.Nonnull;
import mekanism.api.Action;
import mekanism.api.inventory.slot.IInventorySlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

//Like net.minecraftforge.items.SlotItemHandler, except directly interacts with the IInventorySlot instead
public class InventoryContainerSlot extends Slot {

    private static IInventory emptyInventory = new Inventory(0);
    private final ContainerSlotType slotType;
    private final IInventorySlot slot;

    public InventoryContainerSlot(IInventorySlot slot, int index, int x, int y, ContainerSlotType slotType) {
        super(emptyInventory, index, x, y);
        this.slot = slot;
        this.slotType = slotType;
    }

    @Override
    public boolean isItemValid(@Nonnull ItemStack stack) {
        return !stack.isEmpty() && slot.isItemValid(stack);
    }

    @Override
    @Nonnull
    public ItemStack getStack() {
        return slot.getStack();
    }

    @Override
    public void putStack(@Nonnull ItemStack stack) {
        slot.setStack(stack);
        this.onSlotChanged();
    }

    @Override
    public void onSlotChange(@Nonnull ItemStack current, @Nonnull ItemStack newStack) {

    }

    @Override
    public int getSlotStackLimit() {
        return slot.getLimit();
    }

    @Override
    public int getItemStackLimit(@Nonnull ItemStack stack) {
        //TODO: This logic is a lot simpler than the one in SlotItemHandler, is there some case we are missing?
        return Math.min(slot.getLimit(), stack.getMaxStackSize());
    }

    @Override
    public boolean canTakeStack(PlayerEntity playerIn) {
        return !slot.extractItem(1, Action.SIMULATE).isEmpty();
    }

    @Override
    @Nonnull
    public ItemStack decrStackSize(int amount) {
        return slot.extractItem(amount, Action.EXECUTE);
    }

    //TODO: Forge has a TODO for implementing isSameInventory.
    // We can probably implement this by generating a UUID or something when we are looping over our inventory slots creating them
    // and then compare the UUIDs
    /*@Override
    public boolean isSameInventory(Slot other) {
        return other instanceof SlotItemHandler && ((SlotItemHandler) other).getItemHandler() == this.itemHandler;
    }*/

    public ContainerSlotType getSlotType() {
        return slotType;
    }
}