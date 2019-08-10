package mekanism.common.inventory.container;

import mekanism.common.inventory.slot.SlotOutput;
import mekanism.common.tile.fluid_tank.TileEntityFluidTank;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;

public class ContainerFluidTank extends ContainerFluidStorage<TileEntityFluidTank> {

    public ContainerFluidTank(PlayerInventory inventory, TileEntityFluidTank tile) {
        super(tile, inventory);
    }

    @Override
    protected void addSlots() {
        addSlot(new Slot(tileEntity, 0, 146, 19));
        addSlot(new SlotOutput(tileEntity, 1, 146, 51));
    }
}