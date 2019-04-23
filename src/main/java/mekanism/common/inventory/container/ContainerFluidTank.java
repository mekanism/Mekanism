package mekanism.common.inventory.container;

import mekanism.common.inventory.slot.SlotOutput;
import mekanism.common.tile.TileEntityFluidTank;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;

public class ContainerFluidTank extends ContainerFluidStorage<TileEntityFluidTank> {

    public ContainerFluidTank(InventoryPlayer inventory, TileEntityFluidTank tile) {
        super(tile, inventory);
    }

    @Override
    protected void addSlots() {
        addSlotToContainer(new Slot(tileEntity, 0, 146, 19));
        addSlotToContainer(new SlotOutput(tileEntity, 1, 146, 51));
    }
}