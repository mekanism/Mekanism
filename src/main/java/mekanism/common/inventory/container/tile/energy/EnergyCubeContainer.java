package mekanism.common.inventory.container.tile.energy;

import mekanism.common.inventory.container.MekanismContainerTypes;
import mekanism.common.inventory.slot.SlotEnergy.SlotCharge;
import mekanism.common.inventory.slot.SlotEnergy.SlotDischarge;
import mekanism.common.tile.energy_cube.TileEntityEnergyCube;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;

public class EnergyCubeContainer extends MekanismEnergyContainer<TileEntityEnergyCube> {

    public EnergyCubeContainer(int id, PlayerInventory inv, TileEntityEnergyCube tile) {
        super(MekanismContainerTypes.ENERGY_CUBE, id, inv, tile);
    }

    public EnergyCubeContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityEnergyCube.class));
    }

    @Override
    protected void addSlots() {
        addSlot(new SlotCharge(tile, 0, 143, 35));
        addSlot(new SlotDischarge(tile, 1, 17, 35));
    }
}