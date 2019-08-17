package mekanism.common.inventory.container.tile;

import javax.annotation.Nonnull;
import mekanism.common.base.IUpgradeItem;
import mekanism.common.base.IUpgradeTile;
import mekanism.common.inventory.InventoryList;
import mekanism.common.inventory.container.MekanismContainerTypes;
import mekanism.common.inventory.slot.SlotMachineUpgrade;
import mekanism.common.tile.TileEntityQuantumEntangloporter;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

public class UpgradeManagementContainer<TILE extends TileEntityMekanism & IUpgradeTile> extends MekanismTileContainer<TILE> {

    public UpgradeManagementContainer(int id, PlayerInventory inv, TILE tile) {
        super(MekanismContainerTypes.UPGRADE_MANAGEMENT, id, inv, tile);
        //TODO: Fix this
        //Bit of a hack I guess, but we need to give it access to the inventory list, not the Frequency
        IInventory upgradeInv;
        if (tile instanceof TileEntityQuantumEntangloporter) {
            upgradeInv = new InventoryList(tile.getInventory(), tile);
        } else {
            upgradeInv = (TileEntityMekanism) tile;
        }
    }

    public UpgradeManagementContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        //TODO
        this(id, inv, (TILE) getTileFromBuf(buf, TileEntityMekanism.class));
    }

    @Nonnull
    @Override
    public ItemStack transferStackInSlot(PlayerEntity player, int slotID) {
        ItemStack stack = ItemStack.EMPTY;
        Slot currentSlot = inventorySlots.get(slotID);
        if (currentSlot != null && currentSlot.getHasStack()) {
            ItemStack slotStack = currentSlot.getStack();
            stack = slotStack.copy();
            if (slotStack.getItem() instanceof IUpgradeItem) {
                if (slotID != 0) {
                    if (!mergeItemStack(slotStack, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!mergeItemStack(slotStack, 1, inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (slotID >= 1 && slotID <= 27) {
                if (!mergeItemStack(slotStack, 28, inventorySlots.size(), false)) {
                    return ItemStack.EMPTY;
                }
            } else if (slotID > 27) {
                if (!mergeItemStack(slotStack, 1, 27, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!mergeItemStack(slotStack, 1, inventorySlots.size(), true)) {
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
        addSlot(new SlotMachineUpgrade(tile, tile.getComponent().getUpgradeSlot(), 154, 7));
    }
}