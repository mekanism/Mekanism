package mekanism.common.tile.multiblock;

import mekanism.common.lib.multiblock.MultiblockManager;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.prefab.TileEntityStructuralMultiblock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityStructuralGlass extends TileEntityStructuralMultiblock {

    public TileEntityStructuralGlass(BlockPos pos, BlockState state) {
        super(MekanismBlocks.STRUCTURAL_GLASS, pos, state);
    }

    @Override
    public boolean canInterface(MultiblockManager<?> manager) {
        return !manager.getNameLower().contains("reactor");
    }
}
