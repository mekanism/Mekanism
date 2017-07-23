package buildcraft.api.blocks;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface ICustomRotationHandler {
    EnumActionResult attemptRotation(World world, BlockPos pos, IBlockState state, EnumFacing sideWrenched);
}
