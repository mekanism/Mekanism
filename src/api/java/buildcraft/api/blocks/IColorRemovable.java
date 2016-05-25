package buildcraft.api.blocks;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IColorRemovable {
    boolean removeColorFromBlock(World world, BlockPos pos, EnumFacing facing);
}
