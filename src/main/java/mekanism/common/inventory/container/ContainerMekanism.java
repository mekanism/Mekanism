package mekanism.common.inventory.container;

import javax.annotation.Nonnull;
import mekanism.common.tile.base.TileEntityContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;

public abstract class ContainerMekanism<TILE extends TileEntityContainer> extends Container {

    protected TILE tileEntity;

    protected ContainerMekanism(TILE tile, InventoryPlayer inventory) {
        this.tileEntity = tile;
        if (shouldAddSlots()) {
            addSlots();
            if (inventory != null) {
                addInventorySlots(inventory);
                openInventory(inventory);
            }
        }
    }

    protected int getInventoryOffset() {
        return 84;
    }

    protected void addInventorySlots(InventoryPlayer inventory) {
        int offset = getInventoryOffset();
        for (int slotY = 0; slotY < 3; slotY++) {
            for (int slotX = 0; slotX < 9; slotX++) {
                addSlotToContainer(new Slot(inventory, slotX + slotY * 9 + 9, 8 + slotX * 18, offset + slotY * 18));
            }
        }
        offset += 58;
        for (int slotY = 0; slotY < 9; slotY++) {
            addSlotToContainer(new Slot(inventory, slotY, 8 + slotY * 18, offset));
        }
    }

    protected boolean shouldAddSlots() {
        return true;
    }

    protected abstract void addSlots();

    protected void openInventory(InventoryPlayer inventory) {
        if (tileEntity != null) {
            tileEntity.open(inventory.player);
            tileEntity.openInventory(inventory.player);
        }
    }

    @Override
    public void onContainerClosed(EntityPlayer entityplayer) {
        super.onContainerClosed(entityplayer);
        closeInventory(entityplayer);
    }

    protected void closeInventory(EntityPlayer entityplayer) {
        if (tileEntity != null) {
            tileEntity.close(entityplayer);
            tileEntity.closeInventory(entityplayer);
        }
    }

    @Override
    public boolean canInteractWith(@Nonnull EntityPlayer entityplayer) {
        return tileEntity == null || tileEntity.isUsableByPlayer(entityplayer);
    }
}