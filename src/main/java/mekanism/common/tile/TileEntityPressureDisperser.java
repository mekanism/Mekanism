package mekanism.common.tile;

import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityPressureDisperser extends TileEntityMekanism {

    public TileEntityPressureDisperser(BlockPos pos, BlockState state) {
        super(MekanismBlocks.PRESSURE_DISPERSER, pos, state);
    }
}