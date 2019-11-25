package mekanism.common.block.transmitter;

import it.unimi.dsi.fastutil.bytes.Byte2ObjectMap;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectOpenHashMap;
import mekanism.common.tile.transmitter.TileEntitySidedPipe;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;

public abstract class BlockSmallTransmitter extends BlockTransmitter {

    private static final Byte2ObjectMap<VoxelShape> cachedShapes = new Byte2ObjectOpenHashMap<>();
    private static final VoxelShape center;
    public static AxisAlignedBB[] smallSides = new AxisAlignedBB[7];

    static {
        smallSides[0] = new AxisAlignedBB(0.3, 0.0, 0.3, 0.7, 0.3, 0.7);
        smallSides[1] = new AxisAlignedBB(0.3, 0.7, 0.3, 0.7, 1.0, 0.7);
        smallSides[2] = new AxisAlignedBB(0.3, 0.3, 0.0, 0.7, 0.7, 0.3);
        smallSides[3] = new AxisAlignedBB(0.3, 0.3, 0.7, 0.7, 0.7, 1.0);
        smallSides[4] = new AxisAlignedBB(0.0, 0.3, 0.3, 0.3, 0.7, 0.7);
        smallSides[5] = new AxisAlignedBB(0.7, 0.3, 0.3, 1.0, 0.7, 0.7);
        smallSides[6] = new AxisAlignedBB(0.3, 0.3, 0.3, 0.7, 0.7, 0.7);

        center = VoxelShapes.create(smallSides[6]);
    }

    @Override
    protected VoxelShape getCenter() {
        return center;
    }

    @Override
    protected VoxelShape getRealShape(BlockState state, IBlockReader world, BlockPos pos) {
        TileEntitySidedPipe tile = MekanismUtils.getTileEntity(TileEntitySidedPipe.class, world, pos);
        if (tile == null) {
            //If we failed to get the tile, just give the center shape
            return getCenter();
        }
        byte connections = tile.getAllCurrentConnections();
        if (cachedShapes.containsKey(connections)) {
            return cachedShapes.get(connections);
        }
        //If we don't have a cached version of our shape, then we need to calculate it
        VoxelShape current = getCenter();
        for (Direction side : EnumUtils.DIRECTIONS) {
            if (TileEntitySidedPipe.connectionMapContainsSide(connections, side)) {
                current = VoxelShapes.combineAndSimplify(current, VoxelShapes.create(smallSides[side.ordinal()]), IBooleanFunction.OR);
            }
        }
        cachedShapes.put(connections, current);
        return current;
    }
}