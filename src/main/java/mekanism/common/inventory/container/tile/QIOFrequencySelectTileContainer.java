package mekanism.common.inventory.container.tile;

import mekanism.common.inventory.container.IEmptyContainer;
import mekanism.common.registries.MekanismContainerTypes;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;

public class QIOFrequencySelectTileContainer extends MekanismTileContainer<TileEntityMekanism> implements IEmptyContainer {

    public QIOFrequencySelectTileContainer(int id, PlayerInventory inv, TileEntityMekanism tile) {
        super(MekanismContainerTypes.QIO_FREQUENCY_SELECT_TILE, id, inv, tile);
    }

    public QIOFrequencySelectTileContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityMekanism.class));
    }
}
