package mekanism.generators.common.inventory.container.turbine;

import mekanism.common.inventory.container.IEmptyContainer;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.generators.common.registries.GeneratorsContainerTypes;
import mekanism.generators.common.tile.turbine.TileEntityTurbineCasing;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;

public class TurbineStatsContainer extends MekanismTileContainer<TileEntityTurbineCasing> implements IEmptyContainer {

    public TurbineStatsContainer(int id, PlayerInventory inv, TileEntityTurbineCasing tile) {
        super(GeneratorsContainerTypes.TURBINE_STATS, id, inv, tile);
    }

    public TurbineStatsContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityTurbineCasing.class));
    }
}