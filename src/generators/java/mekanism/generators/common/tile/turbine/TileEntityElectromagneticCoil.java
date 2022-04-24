package mekanism.generators.common.tile.turbine;

import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.generators.common.registries.GeneratorsBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityElectromagneticCoil extends TileEntityMekanism {

    public TileEntityElectromagneticCoil(BlockPos pos, BlockState state) {
        super(GeneratorsBlocks.ELECTROMAGNETIC_COIL, pos, state);
    }
}