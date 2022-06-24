package mekanism.common.tile.multiblock;

import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.prefab.TileEntityInternalMultiblock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntitySuperchargedCoil extends TileEntityInternalMultiblock {

    public TileEntitySuperchargedCoil(BlockPos pos, BlockState state) {
        super(MekanismBlocks.SUPERCHARGED_COIL, pos, state);
    }
}
