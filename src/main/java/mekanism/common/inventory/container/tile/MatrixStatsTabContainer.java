package mekanism.common.inventory.container.tile;

import mekanism.common.registries.MekanismContainerTypes;
import mekanism.common.tile.multiblock.TileEntityInductionCasing;
import net.minecraft.entity.player.PlayerInventory;

public class MatrixStatsTabContainer extends EmptyTileContainer<TileEntityInductionCasing> {

    public MatrixStatsTabContainer(int id, PlayerInventory inv, TileEntityInductionCasing tile) {
        super(MekanismContainerTypes.MATRIX_STATS, id, inv, tile);
    }

    @Override
    protected void addContainerTrackers() {
        super.addContainerTrackers();
        tile.addStatsTabContainerTrackers(this);
    }
}