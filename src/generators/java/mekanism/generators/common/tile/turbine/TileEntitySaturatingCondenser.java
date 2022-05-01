package mekanism.generators.common.tile.turbine;

import mekanism.common.tile.prefab.TileEntityInternalMultiblock;
import mekanism.generators.common.registries.GeneratorsBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntitySaturatingCondenser extends TileEntityInternalMultiblock {

    public TileEntitySaturatingCondenser(BlockPos pos, BlockState state) {
        super(GeneratorsBlocks.SATURATING_CONDENSER, pos, state);
    }
}