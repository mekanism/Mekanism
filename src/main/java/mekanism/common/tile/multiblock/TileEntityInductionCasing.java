package mekanism.common.tile.multiblock;

import mekanism.api.providers.IBlockProvider;
import mekanism.common.Mekanism;
import mekanism.common.content.matrix.MatrixMultiblockData;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.dynamic.SyncMapper;
import mekanism.common.lib.multiblock.MultiblockManager;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismContainerTypes;
import mekanism.common.tile.prefab.TileEntityMultiblock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import org.jetbrains.annotations.NotNull;

public class TileEntityInductionCasing extends TileEntityMultiblock<MatrixMultiblockData> {

    public TileEntityInductionCasing(BlockPos pos, BlockState state) {
        this(MekanismBlocks.INDUCTION_CASING, pos, state);
        //Disable item handler caps if we are the induction casing, don't disable it for the subclassed port though
        addDisabledCapabilities(ForgeCapabilities.ITEM_HANDLER);
    }

    public TileEntityInductionCasing(IBlockProvider blockProvider, BlockPos pos, BlockState state) {
        super(blockProvider, pos, state);
    }

    @NotNull
    @Override
    public MatrixMultiblockData createMultiblock() {
        return new MatrixMultiblockData(this);
    }

    @Override
    public MultiblockManager<MatrixMultiblockData> getManager() {
        return Mekanism.matrixManager;
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        if (container.getType() == MekanismContainerTypes.MATRIX_STATS.get()) {
            SyncMapper.INSTANCE.setup(container, MatrixMultiblockData.class, this::getMultiblock, MatrixMultiblockData.STATS_TAB);
        }
    }
}