package mekanism.common.inventory.container.tile;

import mekanism.common.registries.MekanismContainerTypes;
import mekanism.common.tile.multiblock.TileEntityInductionCasing;
import net.minecraft.world.entity.player.Inventory;

public class MatrixStatsTabContainer extends EmptyTileContainer<TileEntityInductionCasing> {

    public MatrixStatsTabContainer(int id, Inventory inv, TileEntityInductionCasing tile) {
        super(MekanismContainerTypes.MATRIX_STATS, id, inv, tile);
    }

    @Override
    protected void addContainerTrackers() {
        super.addContainerTrackers();
        tile.addStatsTabContainerTrackers(this);
    }
}