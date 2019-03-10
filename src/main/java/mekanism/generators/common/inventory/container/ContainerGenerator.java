package mekanism.generators.common.inventory.container;

import javax.annotation.Nonnull;
import mekanism.common.tile.prefab.TileEntityElectricBlock;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;

public abstract class ContainerGenerator extends Container {

    protected TileEntityElectricBlock tileEntity;

    protected ContainerGenerator(InventoryPlayer inventory, TileEntityElectricBlock generator) {
        this.tileEntity = generator;
        addSlots();
        int slotX;

        for (slotX = 0; slotX < 3; slotX++) {
            for (int slotY = 0; slotY < 9; slotY++) {
                addSlotToContainer(new Slot(inventory, slotY + slotX * 9 + 9, 8 + slotY * 18, 84 + slotX * 18));
            }
        }

        for (slotX = 0; slotX < 9; slotX++) {
            addSlotToContainer(new Slot(inventory, slotX, 8 + slotX * 18, 142));
        }

        tileEntity.openInventory(inventory.player);
        tileEntity.open(inventory.player);
    }

    protected abstract void addSlots();

    @Override
    public void onContainerClosed(EntityPlayer entityplayer) {
        super.onContainerClosed(entityplayer);
        tileEntity.closeInventory(entityplayer);
        tileEntity.close(entityplayer);
    }

    @Override
    public boolean canInteractWith(@Nonnull EntityPlayer entityplayer) {
        return tileEntity.isUsableByPlayer(entityplayer);
    }
}