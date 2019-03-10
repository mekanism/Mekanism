package mekanism.common.inventory.container;

import mekanism.common.inventory.slot.SlotEnergy.SlotCharge;
import mekanism.common.inventory.slot.SlotEnergy.SlotDischarge;
import mekanism.common.tile.TileEntityInductionCasing;
import net.minecraft.entity.player.InventoryPlayer;

public class ContainerInductionMatrix extends ContainerEnergyStorage<TileEntityInductionCasing> {

    public ContainerInductionMatrix(InventoryPlayer inventory, TileEntityInductionCasing tile) {
        super(tile, inventory);
    }

    @Override
    protected void addSlots() {
        addSlotToContainer(new SlotCharge(tileEntity, 0, 146, 20));
        addSlotToContainer(new SlotDischarge(tileEntity, 1, 146, 51));
    }
}