package mekanism.generators.common.tile;

import mekanism.common.lib.multiblock.MultiblockManager;
import mekanism.common.tile.prefab.TileEntityStructuralMultiblock;
import mekanism.generators.common.registries.GeneratorsBlocks;

public class TileEntityReactorGlass extends TileEntityStructuralMultiblock {

    public TileEntityReactorGlass() {
        super(GeneratorsBlocks.REACTOR_GLASS);
    }

    @Override
    public boolean canInterface(MultiblockManager<?> manager) {
        return true;
    }
}
