package mekanism.common.inventory.container;

import javax.annotation.Nonnull;
import mekanism.api.gas.IGasItem;
import mekanism.common.inventory.slot.SlotEnergy.SlotDischarge;
import mekanism.common.inventory.slot.SlotStorageTank;
import mekanism.common.tile.TileEntityChemicalInfuser;
import mekanism.common.util.ChargeUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerChemicalInfuser extends ContainerMekanism<TileEntityChemicalInfuser> {

    public ContainerChemicalInfuser(InventoryPlayer inventory, TileEntityChemicalInfuser tile) {
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

            if (ChargeUtils.canBeDischarged(slotStack)) {
                if (slotID != 3) {
                    if (!mergeItemStack(slotStack, 3, 4, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    if (!mergeItemStack(slotStack, 4, inventorySlots.size(), true)) {
                        return ItemStack.EMPTY;
                    }
                }
            } else if (slotStack.getItem() instanceof IGasItem) {
                if (slotID != 0 && slotID != 1 && slotID != 2) {
                    if (!mergeItemStack(slotStack, 0, 3, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    if (!mergeItemStack(slotStack, 5, inventorySlots.size(), true)) {
                        return ItemStack.EMPTY;
                    }
                }
            } else {
                if (slotID >= 4 && slotID <= 30) {
                    if (!mergeItemStack(slotStack, 31, inventorySlots.size(), false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (slotID > 30) {
                    if (!mergeItemStack(slotStack, 4, 30, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    if (!mergeItemStack(slotStack, 4, inventorySlots.size(), true)) {
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

    @Override
    protected void addSlots() {
        addSlotToContainer(new SlotStorageTank(tileEntity, 0, 5, 56));
        addSlotToContainer(new SlotStorageTank(tileEntity, 1, 155, 56));
        addSlotToContainer(new SlotStorageTank(tileEntity, 2, 80, 65));
        addSlotToContainer(new SlotDischarge(tileEntity, 3, 155, 5));
    }
}
