package mekanism.common.inventory.container.tile;

import mekanism.common.inventory.container.MekanismContainerTypes;
import mekanism.common.tile.TileEntitySolarNeutronActivator;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;

public class SolarNeutronActivatorContainer extends MekanismTileContainer<TileEntitySolarNeutronActivator> {

    public SolarNeutronActivatorContainer(int id, PlayerInventory inv, TileEntitySolarNeutronActivator tile) {
        super(MekanismContainerTypes.SOLAR_NEUTRON_ACTIVATOR, id, inv, tile);
    }

    public SolarNeutronActivatorContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntitySolarNeutronActivator.class));
    }
}