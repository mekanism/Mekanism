package mekanism.common.inventory.container.tile;

import mekanism.common.inventory.container.MekanismContainerTypes;
import mekanism.common.tile.TileEntityGasTank;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;

public class GasTankContainer extends MekanismTileContainer<TileEntityGasTank> {

    public GasTankContainer(int id, PlayerInventory inv, TileEntityGasTank tile) {
        super(MekanismContainerTypes.GAS_TANK, id, inv, tile);
    }

    public GasTankContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityGasTank.class));
    }
}