package mekanism.common.inventory.container.tile;

import mekanism.common.registries.MekanismContainerTypes;
import mekanism.common.tile.TileEntityFuelwoodHeater;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;

public class FuelwoodHeaterContainer extends MekanismTileContainer<TileEntityFuelwoodHeater> {

    public FuelwoodHeaterContainer(int id, PlayerInventory inv, TileEntityFuelwoodHeater tile) {
        super(MekanismContainerTypes.FUELWOOD_HEATER, id, inv, tile);
    }

    public FuelwoodHeaterContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityFuelwoodHeater.class));
    }
}