package mekanism.common.inventory.container.tile;

import mekanism.common.inventory.container.IEmptyContainer;
import mekanism.common.inventory.container.MekanismContainerTypes;
import mekanism.common.tile.TileEntityInductionCasing;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;

public class MatrixStatsContainer extends MekanismTileContainer<TileEntityInductionCasing> implements IEmptyContainer {

    public MatrixStatsContainer(int id, PlayerInventory inv, TileEntityInductionCasing tile) {
        super(MekanismContainerTypes.MATRIX_STATS, id, inv, tile);
    }

    public MatrixStatsContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityInductionCasing.class));
    }
}