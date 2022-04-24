package mekanism.generators.common.tile;

import mekanism.common.lib.multiblock.MultiblockManager;
import mekanism.common.tile.prefab.TileEntityStructuralMultiblock;
import mekanism.generators.common.registries.GeneratorsBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityReactorGlass extends TileEntityStructuralMultiblock {

    public TileEntityReactorGlass(BlockPos pos, BlockState state) {
        super(GeneratorsBlocks.REACTOR_GLASS, pos, state);
    }

    @Override
    public boolean canInterface(MultiblockManager<?> manager) {
        return true;
    }
}
