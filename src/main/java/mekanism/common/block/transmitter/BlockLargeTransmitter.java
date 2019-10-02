package mekanism.common.block.transmitter;

import it.unimi.dsi.fastutil.bytes.Byte2ObjectMap;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectOpenHashMap;
import mekanism.common.tile.transmitter.TileEntitySidedPipe;
import mekanism.common.util.EnumUtils;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;

public abstract class BlockLargeTransmitter extends BlockTransmitter {

    private static final Byte2ObjectMap<VoxelShape> cachedShapes = new Byte2ObjectOpenHashMap<>();
    private static final VoxelShape center;
    public static AxisAlignedBB[] largeSides = new AxisAlignedBB[7];

    static {
        largeSides[0] = new AxisAlignedBB(0.25, 0.0, 0.25, 0.75, 0.25, 0.75);
        largeSides[1] = new AxisAlignedBB(0.25, 0.75, 0.25, 0.75, 1.0, 0.75);
        largeSides[2] = new AxisAlignedBB(0.25, 0.25, 0.0, 0.75, 0.75, 0.25);
        largeSides[3] = new AxisAlignedBB(0.25, 0.25, 0.75, 0.75, 0.75, 1.0);
        largeSides[4] = new AxisAlignedBB(0.0, 0.25, 0.25, 0.25, 0.75, 0.75);
        largeSides[5] = new AxisAlignedBB(0.75, 0.25, 0.25, 1.0, 0.75, 0.75);
        largeSides[6] = new AxisAlignedBB(0.25, 0.25, 0.25, 0.75, 0.75, 0.75);

        center = VoxelShapes.create(largeSides[6]);
    }

    protected BlockLargeTransmitter(String name) {
        super(name);
    }

    @Override
    protected VoxelShape getCenter() {
        return center;
    }

    @Override
    protected VoxelShape getRealShape(BlockState state, IBlockReader world, BlockPos pos) {
        TileEntitySidedPipe tile = getTileEntitySidedPipe(world, pos);
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
                current = VoxelShapes.combineAndSimplify(current, VoxelShapes.create(largeSides[side.ordinal()]), IBooleanFunction.OR);
            }
        }
        cachedShapes.put(connections, current);
        return current;
    }
}