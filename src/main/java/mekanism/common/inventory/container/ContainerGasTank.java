package mekanism.common.inventory.container;

import javax.annotation.Nonnull;
import mekanism.api.gas.IGasItem;
import mekanism.common.inventory.slot.SlotStorageTank;
import mekanism.common.tile.TileEntityGasTank;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerGasTank extends ContainerMekanism<TileEntityGasTank> {

    public ContainerGasTank(InventoryPlayer inventory, TileEntityGasTank tile) {
        super(tile, inventory);
    }

    @Nonnull
    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int slotID) {
        ItemStack stack = ItemStack.EMPTY;
        Slot currentSlot = inventorySlots.get(slotID);
        if (currentSlot != null && currentSlot.getHasStack()) {
            ItemStack slotStack = currentSlot.getStack();
            stack = slotStack.copy();
            if (slotStack.getItem() instanceof IGasItem) {
                if (slotID != 0 && slotID != 1) {
                    if (!mergeItemStack(slotStack, 0, 1, false)) {
                        if (!mergeItemStack(slotStack, 1, 2, false)) {
                            return ItemStack.EMPTY;
                        }
                    }
                } else if (!mergeItemStack(slotStack, 2, inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (slotID >= 2 && slotID <= 28) {
                if (!mergeItemStack(slotStack, 29, inventorySlots.size(), false)) {
                    return ItemStack.EMPTY;
                }
            } else if (slotID > 28) {
                if (!mergeItemStack(slotStack, 2, 28, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!mergeItemStack(slotStack, 2, inventorySlots.size(), true)) {
                return ItemStack.EMPTY;
            }
            if (slotStack.getCount() == 0) {
                currentSlot.putStack(ItemStack.EMPTY);
            } else {
                currentSlot.onSlotChanged();
            }
            if (slotStack.getCount() == stack.getCount()) {
                return ItemStack.EMPTY;
            }
            currentSlot.onTake(player, slotStack);
        }
        return stack;
    }

    @Override
    protected void addSlots() {
        addSlotToContainer(new SlotStorageTank(tileEntity, 0, 8, 8));
        addSlotToContainer(new SlotStorageTank(tileEntity, 1, 8, 40));
    }
}