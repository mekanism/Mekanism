package mekanism.generators.common.tile.fusion;

import mekanism.generators.common.registries.GeneratorsBlocks;

public class TileEntityFusionReactorGlass extends TileEntityFusionReactorBlock {

    public TileEntityFusionReactorGlass() {
        super(GeneratorsBlocks.FUSION_REACTOR_GLASS);
    }

    @Override
    public boolean isFrame() {
        return false;
    }
}