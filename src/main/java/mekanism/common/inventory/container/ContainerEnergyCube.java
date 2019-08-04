package mekanism.common.inventory.container;

import mekanism.common.inventory.slot.SlotEnergy.SlotCharge;
import mekanism.common.inventory.slot.SlotEnergy.SlotDischarge;
import mekanism.common.tile.energy_cube.TileEntityEnergyCube;
import net.minecraft.entity.player.InventoryPlayer;

public class ContainerEnergyCube extends ContainerEnergyStorage<TileEntityEnergyCube> {

    public ContainerEnergyCube(InventoryPlayer inventory, TileEntityEnergyCube tile) {
        super(tile, inventory);
    }

    @Override
    protected void addSlots() {
        addSlotToContainer(new SlotCharge(tileEntity, 0, 143, 35));
        addSlotToContainer(new SlotDischarge(tileEntity, 1, 17, 35));
    }
}