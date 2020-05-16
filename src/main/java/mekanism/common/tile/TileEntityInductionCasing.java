package mekanism.common.tile;

import javax.annotation.Nonnull;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.Mekanism;
import mekanism.common.content.matrix.MatrixMultiblockData;
import mekanism.common.content.matrix.MatrixUpdateProtocol;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.dynamic.SyncMapper;
import mekanism.common.lib.multiblock.MultiblockManager;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.prefab.TileEntityMultiblock;
import net.minecraftforge.items.CapabilityItemHandler;

public class TileEntityInductionCasing extends TileEntityMultiblock<MatrixMultiblockData> {

    public TileEntityInductionCasing() {
        this(MekanismBlocks.INDUCTION_CASING);
        //Disable item handler caps if we are the induction casing, don't disable it for the subclassed port though
        addDisabledCapabilities(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
    }

    public TileEntityInductionCasing(IBlockProvider blockProvider) {
        super(blockProvider);
    }

    @Nonnull
    @Override
    public MatrixMultiblockData getNewStructure() {
        return new MatrixMultiblockData(this);
    }

    @Override
    public MatrixUpdateProtocol getProtocol() {
        return new MatrixUpdateProtocol(this);
    }

    @Override
    public MultiblockManager<MatrixMultiblockData> getManager() {
        return Mekanism.matrixManager;
    }

    public void addStatsTabContainerTrackers(MekanismContainer container) {
        SyncMapper.setup(container, getMultiblock().getClass(), () -> getMultiblock(), "stats");
    }
}