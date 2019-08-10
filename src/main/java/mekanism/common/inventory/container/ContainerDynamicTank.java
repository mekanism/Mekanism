package mekanism.common.inventory.container;

import mekanism.common.inventory.slot.SlotOutput;
import mekanism.common.tile.TileEntityDynamicTank;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;

public class ContainerDynamicTank extends ContainerFluidStorage<TileEntityDynamicTank> {

    public ContainerDynamicTank(PlayerInventory inventory, TileEntityDynamicTank tile) {
        super(tile, inventory);
    }

    @Override
    protected void addSlots() {
        addSlot(new Slot(tileEntity, 0, 146, 20));
        addSlot(new SlotOutput(tileEntity, 1, 146, 51));
    }
}