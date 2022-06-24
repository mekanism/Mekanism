package mekanism.common.tile;

import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.prefab.TileEntityInternalMultiblock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityPressureDisperser extends TileEntityInternalMultiblock {

    public TileEntityPressureDisperser(BlockPos pos, BlockState state) {
        super(MekanismBlocks.PRESSURE_DISPERSER, pos, state);
    }
}