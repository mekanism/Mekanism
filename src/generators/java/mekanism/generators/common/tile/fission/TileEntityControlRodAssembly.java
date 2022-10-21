package mekanism.generators.common.tile.fission;

import mekanism.generators.common.registries.GeneratorsBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityControlRodAssembly extends TileEntityFissionAssembly {

    public TileEntityControlRodAssembly(BlockPos pos, BlockState state) {
        super(GeneratorsBlocks.CONTROL_ROD_ASSEMBLY, pos, state);
    }
}