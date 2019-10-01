package mekanism.common.block.transmitter;

import javax.annotation.Nonnull;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;

public abstract class BlockLargeTransmitter extends BlockTransmitter {

    public static AxisAlignedBB[] largeSides = new AxisAlignedBB[7];

    public static AxisAlignedBB largeDefault;

    static {
        largeSides[0] = new AxisAlignedBB(0.25, 0.0, 0.25, 0.75, 0.25, 0.75);
        largeSides[1] = new AxisAlignedBB(0.25, 0.75, 0.25, 0.75, 1.0, 0.75);
        largeSides[2] = new AxisAlignedBB(0.25, 0.25, 0.0, 0.75, 0.75, 0.25);
        largeSides[3] = new AxisAlignedBB(0.25, 0.25, 0.75, 0.75, 0.75, 1.0);
        largeSides[4] = new AxisAlignedBB(0.0, 0.25, 0.25, 0.25, 0.75, 0.75);
        largeSides[5] = new AxisAlignedBB(0.75, 0.25, 0.25, 1.0, 0.75, 0.75);
        largeSides[6] = new AxisAlignedBB(0.25, 0.25, 0.25, 0.75, 0.75, 0.75);

        largeDefault = largeSides[6];
    }

    protected BlockLargeTransmitter(String name) {
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
            largeDefault = result.bounds;
        }
        return result != null ? result.hit : null;
    }*/

    @Nonnull
    @Override
    @Deprecated
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        //TODO: This is probably not performant compared to caching it
        //TODO: Is this even returning the correct value?
        return VoxelShapes.create(BlockLargeTransmitter.largeSides[6]);
    }

    //TODO: Implement this as it seems to be incorrect (for the edges)
    /*@Nonnull
    @Override
    @Deprecated
    public AxisAlignedBB getSelectedBoundingBox(BlockState state, @Nonnull World world, @Nonnull BlockPos pos) {
        return largeDefault.offset(pos);
    }*/
}