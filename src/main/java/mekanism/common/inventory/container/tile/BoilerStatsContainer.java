package mekanism.common.inventory.container.tile;

import mekanism.common.inventory.container.IEmptyContainer;
import mekanism.common.inventory.container.MekanismContainerTypes;
import mekanism.common.tile.TileEntityBoilerCasing;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;

public class BoilerStatsContainer extends MekanismTileContainer<TileEntityBoilerCasing> implements IEmptyContainer {

    public BoilerStatsContainer(int id, PlayerInventory inv, TileEntityBoilerCasing tile) {
        super(MekanismContainerTypes.BOILER_STATS, id, inv, tile);
    }

    public BoilerStatsContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityBoilerCasing.class));
    }
}