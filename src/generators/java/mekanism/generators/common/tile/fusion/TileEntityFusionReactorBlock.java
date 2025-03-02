package mekanism.generators.common.tile.fusion;

import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.dynamic.SyncMapper;
import mekanism.common.lib.multiblock.MultiblockManager;
import mekanism.common.tile.prefab.TileEntityMultiblock;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.content.fusion.FusionReactorMultiblockData;
import mekanism.generators.common.registries.GeneratorsBlocks;
import mekanism.generators.common.registries.GeneratorsContainerTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityFusionReactorBlock extends TileEntityMultiblock<FusionReactorMultiblockData> {

    public TileEntityFusionReactorBlock(BlockPos pos, BlockState state) {
        this(GeneratorsBlocks.FUSION_REACTOR_FRAME, pos, state);
    }

    public TileEntityFusionReactorBlock(Holder<Block> blockProvider, BlockPos pos, BlockState state) {
        super(blockProvider, pos, state);
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
    public boolean canBeMaster() {
        return false;
    }

    public void setInjectionRateFromPacket(int rate) {
        FusionReactorMultiblockData multiblock = getMultiblock();
        if (multiblock.isFormed()) {
            multiblock.setInjectionRate(Mth.clamp(rate - (rate % 2), 0, FusionReactorMultiblockData.MAX_INJECTION));
            markForSave();
        }
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        if (container.getType() == GeneratorsContainerTypes.FUSION_REACTOR_FUEL.get()) {
            addTabContainerTracker(container, FusionReactorMultiblockData.FUEL_TAB);
        } else if (container.getType() == GeneratorsContainerTypes.FUSION_REACTOR_HEAT.get()) {
            addTabContainerTracker(container, FusionReactorMultiblockData.HEAT_TAB);
        } else if (container.getType() == GeneratorsContainerTypes.FUSION_REACTOR_STATS.get()) {
            addTabContainerTracker(container, FusionReactorMultiblockData.STATS_TAB);
        }
    }

    private void addTabContainerTracker(MekanismContainer container, String tab) {
        SyncMapper.INSTANCE.setup(container, FusionReactorMultiblockData.class, this::getMultiblock, tab);
    }
}