package buildcraft.api.transport;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public interface ICustomPipeConnection {
    /** @return How long the connecting pipe should extend for. Values less than -4/16 indicate that the pipe will not
     *         connect at all, and will render as it it was not connected. */
    public float getExtension(World world, BlockPos pos, EnumFacing face, IBlockState state);
}
