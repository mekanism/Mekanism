package mekanism.generators.common.tile.turbine;

import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.generators.common.registries.GeneratorsBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntitySaturatingCondenser extends TileEntityMekanism {

    public TileEntitySaturatingCondenser(BlockPos pos, BlockState state) {
        super(GeneratorsBlocks.SATURATING_CONDENSER, pos, state);
    }
}