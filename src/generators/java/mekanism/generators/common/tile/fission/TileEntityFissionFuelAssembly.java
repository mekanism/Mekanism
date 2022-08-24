package mekanism.generators.common.tile.fission;

import mekanism.generators.common.registries.GeneratorsBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityFissionFuelAssembly extends TileEntityFissionAssembly {

    public TileEntityFissionFuelAssembly(BlockPos pos, BlockState state) {
        super(GeneratorsBlocks.FISSION_FUEL_ASSEMBLY, pos, state);
    }
}