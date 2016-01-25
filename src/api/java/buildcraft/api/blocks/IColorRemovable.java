package buildcraft.api.blocks;

import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public interface IColorRemovable {
    boolean removeColorFromBlock(World world, BlockPos pos, EnumFacing facing);
}
