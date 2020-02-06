package mekanism.generators.common.tile.reactor;

import mekanism.generators.common.registries.GeneratorsBlocks;

public class TileEntityReactorGlass extends TileEntityReactorBlock {

    public TileEntityReactorGlass() {
        super(GeneratorsBlocks.REACTOR_GLASS);
    }

    @Override
    public boolean isFrame() {
        return false;
    }
}