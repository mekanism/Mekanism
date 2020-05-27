package mekanism.generators.common.tile.fusion;

import mekanism.api.providers.IBlockProvider;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.dynamic.SyncMapper;
import mekanism.common.lib.multiblock.MultiblockManager;
import mekanism.common.tile.prefab.TileEntityMultiblock;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.content.fusion.FusionReactorMultiblockData;
import mekanism.generators.common.registries.GeneratorsBlocks;

public class TileEntityFusionReactorBlock extends TileEntityMultiblock<FusionReactorMultiblockData> {

    public TileEntityFusionReactorBlock() {
        this(GeneratorsBlocks.FUSION_REACTOR_FRAME);
    }

    public TileEntityFusionReactorBlock(IBlockProvider blockProvider) {
        super(blockProvider);
    }

    @Override
    public FusionReactorMultiblockData createMultiblock() {
        return new FusionReactorMultiblockData(this);
    }

    @Override
    public MultiblockManager<FusionReactorMultiblockData> getManager() {
        return MekanismGenerators.fusionReactorManager;
    }

    @Override
    protected boolean canBeMaster() {
        return false;
    }

    public void setInjectionRateFromPacket(int rate) {
        if (getMultiblock().isFormed()) {
            getMultiblock().setInjectionRate(Math.min(FusionReactorMultiblockData.MAX_INJECTION, Math.max(0, rate - (rate % 2))));
            markDirty(false);
        }
    }

    public void addFuelTabContainerTrackers(MekanismContainer container) {
        SyncMapper.setup(container, FusionReactorMultiblockData.class, this::getMultiblock, "fuel");
    }

    public void addHeatTabContainerTrackers(MekanismContainer container) {
        SyncMapper.setup(container, FusionReactorMultiblockData.class, this::getMultiblock, "heat");
    }
}