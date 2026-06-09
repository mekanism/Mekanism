package mekanism.common.tile.multiblock;

import mekanism.common.content.matrix.MatrixMultiblockData;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.dynamic.SyncMapper;
import mekanism.common.lib.multiblock.MekanismMultiblocks;
import mekanism.common.lib.multiblock.MultiblockType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismContainerTypes;
import mekanism.common.tile.prefab.TileEntityMultiblock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityInductionCasing extends TileEntityMultiblock<MatrixMultiblockData> {

    public TileEntityInductionCasing(BlockPos pos, BlockState state) {
        this(MekanismBlocks.INDUCTION_CASING, pos, state);
    }

    public TileEntityInductionCasing(Holder<Block> blockProvider, BlockPos pos, BlockState state) {
        super(blockProvider, pos, state);
    }

    @Override
    public MatrixMultiblockData createMultiblock() {
        return new MatrixMultiblockData(this);
    }

    @Override
    public MultiblockType<MatrixMultiblockData> getMultiblockType() {
        return MekanismMultiblocks.MATRIX;
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        if (container.getType() == MekanismContainerTypes.MATRIX_STATS.get()) {
            SyncMapper.INSTANCE.setup(container, MatrixMultiblockData.class, this::getMultiblock, MatrixMultiblockData.STATS_TAB);
        }
    }
}