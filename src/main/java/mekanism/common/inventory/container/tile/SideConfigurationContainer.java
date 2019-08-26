package mekanism.common.inventory.container.tile;

import mekanism.common.inventory.container.IEmptyContainer;
import mekanism.common.inventory.container.MekanismContainerTypes;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;

public class SideConfigurationContainer extends MekanismTileContainer<TileEntityMekanism> implements IEmptyContainer {

    public SideConfigurationContainer(int id, PlayerInventory inv, TileEntityMekanism tile) {
        super(MekanismContainerTypes.SIDE_CONFIGURATION, id, inv, tile);
    }

    public SideConfigurationContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityMekanism.class));
    }
}