package mekanism.common.inventory.container;

import mekanism.common.inventory.slot.SlotOutput;
import mekanism.common.tile.TileEntityDynamicTank;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;

public class ContainerDynamicTank extends ContainerFluidStorage<TileEntityDynamicTank> {

    public ContainerDynamicTank(InventoryPlayer inventory, TileEntityDynamicTank tile) {
        super(tile, inventory);
    }

    @Override
    protected void addSlots() {
        addSlotToContainer(new Slot(tileEntity, 0, 146, 20));
        addSlotToContainer(new SlotOutput(tileEntity, 1, 146, 51));
    }
}