package mekanism.common.inventory.container.tile;

import mekanism.common.registries.MekanismContainerTypes;
import mekanism.common.tile.TileEntityDynamicTank;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;

public class DynamicTankContainer extends MekanismTileContainer<TileEntityDynamicTank> {

    public DynamicTankContainer(int id, PlayerInventory inv, TileEntityDynamicTank tile) {
        super(MekanismContainerTypes.DYNAMIC_TANK, id, inv, tile);
    }

    public DynamicTankContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityDynamicTank.class));
    }
}