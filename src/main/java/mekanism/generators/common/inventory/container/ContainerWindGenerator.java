package mekanism.generators.common.inventory.container;

import mekanism.common.inventory.slot.SlotEnergy.SlotCharge;
import mekanism.common.util.ChargeUtils;
import mekanism.generators.common.tile.TileEntityWindGenerator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerWindGenerator extends Container {

    private TileEntityWindGenerator tileEntity;

    public ContainerWindGenerator(InventoryPlayer inventory, TileEntityWindGenerator tentity) {
        tileEntity = tentity;
        addSlotToContainer(new SlotCharge(tentity, 0, 143, 35));
        int slotX;

        for (slotX = 0; slotX < 3; ++slotX) {
            for (int slotY = 0; slotY < 9; ++slotY) {
                addSlotToContainer(new Slot(inventory, slotY + slotX * 9 + 9, 8 + slotY * 18, 84 + slotX * 18));
            }
        }

        for (slotX = 0; slotX < 9; ++slotX) {
            addSlotToContainer(new Slot(inventory, slotX, 8 + slotX * 18, 142));
        }

        tileEntity.openInventory(inventory.player);
        tileEntity.open(inventory.player);
    }

    @Override
    public void onContainerClosed(EntityPlayer entityplayer) {
        super.onContainerClosed(entityplayer);

        tileEntity.closeInventory(entityplayer);
        tileEntity.close(entityplayer);
    }

    @Override
    public boolean canInteractWith(EntityPlayer entityplayer) {
        return tileEntity.isUsableByPlayer(entityplayer);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int slotID) {
        ItemStack stack = ItemStack.EMPTY;
        Slot currentSlot = (Slot) inventorySlots.get(slotID);

        if (currentSlot != null && currentSlot.getHasStack()) {
            ItemStack slotStack = currentSlot.getStack();
            stack = slotStack.copy();

            if (ChargeUtils.canBeCharged(slotStack)) {
                if (slotID != 0) {
                    if (!mergeItemStack(slotStack, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (slotID == 0) {
                    if (!mergeItemStack(slotStack, 1, inventorySlots.size(), false)) {
                        return ItemStack.EMPTY;
                    }
                }
            } else {
                if (slotID >= 1 && slotID <= 27) {
                    if (!mergeItemStack(slotStack, 28, inventorySlots.size(), false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (slotID > 27) {
                    if (!mergeItemStack(slotStack, 1, 27, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    if (!mergeItemStack(slotStack, 1, inventorySlots.size(), true)) {
                        return ItemStack.EMPTY;
                    }
                }
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
}
