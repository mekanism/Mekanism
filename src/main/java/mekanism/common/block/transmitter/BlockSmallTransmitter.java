package mekanism.common.block.transmitter;

import javax.annotation.Nonnull;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;

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

    /*@Override
    @Deprecated
    public RayTraceResult collisionRayTrace(BlockState blockState, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull Vec3d start, @Nonnull Vec3d end) {
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
    }*/

    @Nonnull
    @Override
    @Deprecated
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        //TODO: This is probably not performant compared to caching it
        //TODO: Is this even returning the correct value?
        return VoxelShapes.create(BlockSmallTransmitter.smallSides[6]);
    }

    /*@Nonnull
    @Override
    @Deprecated
    public AxisAlignedBB getSelectedBoundingBox(BlockState state, @Nonnull World world, @Nonnull BlockPos pos) {
        return smallDefault.offset(pos);
    }*/
}