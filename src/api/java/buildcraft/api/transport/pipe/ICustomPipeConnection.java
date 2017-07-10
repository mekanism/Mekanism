package buildcraft.api.transport.pipe;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface ICustomPipeConnection {
    /** @return How long the connecting pipe should extend for, in addition to its normal 4/16f connection. Values less
     *         than or equal to <code>-4 / 16.0f</code> indicate that the pipe will not connect at all, and will render
     *         as it it was not connected. */
    float getExtension(World world, BlockPos pos, EnumFacing face, IBlockState state);
}
