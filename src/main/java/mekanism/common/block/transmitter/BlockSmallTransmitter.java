package mekanism.common.block.transmitter;

import java.util.List;
import javax.annotation.Nonnull;
import mekanism.common.tile.transmitter.TileEntitySidedPipe;
import mekanism.common.util.MultipartUtils;
import mekanism.common.util.MultipartUtils.AdvancedRayTraceResult;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public abstract class BlockSmallTransmitter extends BlockTransmitter {

    public static AxisAlignedBB[] smallSides = new AxisAlignedBB[7];

    public static AxisAlignedBB smallDefault;

    static {
        smallSides[0] = new AxisAlignedBB(0.3, 0.0, 0.3, 0.7, 0.3, 0.7);
        smallSides[1] = new AxisAlignedBB(0.3, 0.7, 0.3, 0.7, 1.0, 0.7);
        smallSides[2] = new AxisAlignedBB(0.3, 0.3, 0.0, 0.7, 0.7, 0.3);
        smallSides[3] = new AxisAlignedBB(0.3, 0.3, 0.7, 0.7, 0.7, 1.0);
        smallSides[4] = new AxisAlignedBB(0.0, 0.3, 0.3, 0.3, 0.7, 0.7);
        smallSides[5] = new AxisAlignedBB(0.7, 0.3, 0.3, 1.0, 0.7, 0.7);
        smallSides[6] = new AxisAlignedBB(0.3, 0.3, 0.3, 0.7, 0.7, 0.7);

        smallDefault = smallSides[6];
    }

    protected BlockSmallTransmitter(String name) {
        super(name);
    }

    @Override
    @Deprecated
    public RayTraceResult collisionRayTrace(IBlockState blockState, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull Vec3d start, @Nonnull Vec3d end) {
        TileEntitySidedPipe tile = getTileEntitySidedPipe(world, pos);
        if (tile == null) {
            return null;
        }
        List<AxisAlignedBB> boxes = tile.getCollisionBoxes();
        AdvancedRayTraceResult result = MultipartUtils.collisionRayTrace(pos, start, end, boxes);
        if (result != null && result.valid()) {
            smallDefault = result.bounds;
        }
        return result != null ? result.hit : null;
    }

    @Nonnull
    @Override
    @Deprecated
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        return BlockSmallTransmitter.smallSides[6];
    }

    @Nonnull
    @Override
    @Deprecated
    public AxisAlignedBB getSelectedBoundingBox(IBlockState state, @Nonnull World world, @Nonnull BlockPos pos) {
        return smallDefault.offset(pos);
    }
}