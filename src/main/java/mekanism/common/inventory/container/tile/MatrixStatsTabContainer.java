package mekanism.common.inventory.container.tile;

import mekanism.common.registries.MekanismContainerTypes;
import mekanism.common.tile.multiblock.TileEntityInductionCasing;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;

public class MatrixStatsTabContainer extends EmptyTileContainer<TileEntityInductionCasing> {

    public MatrixStatsTabContainer(int id, PlayerInventory inv, TileEntityInductionCasing tile) {
        super(MekanismContainerTypes.MATRIX_STATS, id, inv, tile);
    }

    public MatrixStatsTabContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityInductionCasing.class));
    }

    @Override
    protected void addContainerTrackers() {
        super.addContainerTrackers();
        if (tile != null) {
            tile.addStatsTabContainerTrackers(this);
        }
    }
}