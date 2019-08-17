package mekanism.common.inventory.container.tile;

import mekanism.common.base.ISideConfiguration;
import mekanism.common.inventory.container.IEmptyContainer;
import mekanism.common.inventory.container.MekanismContainerTypes;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;

public class SideConfigurationContainer<TILE extends TileEntityMekanism & ISideConfiguration> extends MekanismTileContainer<TILE> implements IEmptyContainer {

    public SideConfigurationContainer(int id, PlayerInventory inv, TILE tile) {
        super(MekanismContainerTypes.SIDE_CONFIGURATION, id, inv, tile);
    }

    public SideConfigurationContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        //TODO
        this(id, inv, (TILE) getTileFromBuf(buf, TileEntityMekanism.class));
    }
}