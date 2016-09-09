package mekanism.client.render.ctm;

import static net.minecraft.util.EnumFacing.DOWN;
import static net.minecraft.util.EnumFacing.EAST;
import static net.minecraft.util.EnumFacing.NORTH;
import static net.minecraft.util.EnumFacing.SOUTH;
import static net.minecraft.util.EnumFacing.UP;
import static net.minecraft.util.EnumFacing.WEST;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

/**
 * Think of this class as a "Two dimensional ForgeDirection, with diagonals".
 * <p>
 * It represents the eight different directions a face of a block can connect with CTM, and contains the logic for determining if a block is indeed connected in that direction.
 * <p>
 * Note that, for example, {@link #TOP_RIGHT} does not mean connected to the {@link #TOP} and {@link #RIGHT}, but connected in the diagonal direction represented by {@link #TOP_RIGHT}. This is used
 * for inner corner rendering.
 */
public enum Dir {
	// @formatter:off
    TOP(UP), 
    TOP_RIGHT(UP, EAST), 
    RIGHT(EAST), 
    BOTTOM_RIGHT(DOWN, EAST), 
    BOTTOM(DOWN), 
    BOTTOM_LEFT(DOWN, WEST), 
    LEFT(WEST), 
    TOP_LEFT(UP, WEST);
    // @formatter:on

	/**
	 * All values of this enum, used to prevent unnecessary allocation via {@link #values()}.
	 */
	public static final Dir[] VALUES = values();
	private static final EnumFacing NORMAL = SOUTH;

	private EnumFacing[] dirs;

	private Dir(EnumFacing... dirs) {
		this.dirs = dirs;
    }

    /**
     * Finds if this block is connected for the given side in this Dir.
     * 
     * @param inst
     *            The CTM instance to use for logic.
     * @param world
     *            The world the block is in.
     * @param pos
     *            The position of your block.
     * @param side
     *            The side of the current face.
     * @return True if the block is connected in the given Dir, false otherwise.
     */
    public boolean isConnected(CTM ctm, IBlockAccess world, BlockPos pos, EnumFacing side) {
        return ctm.isConnected(world, pos, getConnection(pos, side), side);
    }

    /**
     * Finds if this block is connected for the given side in this Dir.
     * 
     * @param inst
     *            The CTM instance to use for logic.
     * @param world
     *            The world the block is in.
     * @param pos
     *            The position of your block.
     * @param side
     *            The side of the current face.
     * @param side
     *            The state to check for connection with.
     * @return True if the block is connected in the given Dir, false otherwise.
     */
    public boolean isConnected(CTM ctm, IBlockAccess world, BlockPos pos, EnumFacing side, IBlockState state) {
        return ctm.isConnected(world, pos, getConnection(pos, side), side, state);
    }
    
    private BlockPos getConnection(BlockPos pos, EnumFacing side) {
        EnumFacing[] dirs = getNormalizedDirs(side);
        BlockPos connection = pos;
        for (EnumFacing dir : dirs) {
            connection = connection.offset(dir);
        }
        return connection;
    }

	public EnumFacing[] getNormalizedDirs(EnumFacing normal) {
		if (normal == NORMAL) {
			return dirs;
		} else if (normal == NORMAL.getOpposite()) {
			// If this is the opposite direction of the default normal, we
			// need to mirror the dirs
			// A mirror version does not affect y+ and y- so we ignore those
			EnumFacing[] ret = new EnumFacing[dirs.length];
			for (int i = 0; i < ret.length; i++) {
				ret[i] = dirs[i].getFrontOffsetY() != 0 ? dirs[i] : dirs[i].getOpposite();
			}
			return ret;
		} else {
			EnumFacing axis = null;
			// Next, we need different a different rotation axis depending
			// on if this is up/down or not
			if (normal.getFrontOffsetY() == 0) {
				// If it is not up/down, pick either the left or right-hand
				// rotation
				axis = normal == NORMAL.rotateY() ? UP : DOWN;
			} else {
				// If it is up/down, pick either the up or down rotation.
				axis = normal == UP ? NORMAL.rotateYCCW() : NORMAL.rotateY();
			}
			EnumFacing[] ret = new EnumFacing[dirs.length];
			// Finally apply all the rotations
			for (int i = 0; i < ret.length; i++) {
				ret[i] = rotate(dirs[i], axis);
			}
			return ret;
		}
	}

	// God why

	private static final int[] FACING_LOOKUP = new int[EnumFacing.values().length];
	static {
		FACING_LOOKUP[NORTH.ordinal()] = 1;
		FACING_LOOKUP[EAST.ordinal()] = 2;
		FACING_LOOKUP[SOUTH.ordinal()] = 3;
		FACING_LOOKUP[WEST.ordinal()] = 4;
		FACING_LOOKUP[UP.ordinal()] = 5;
		FACING_LOOKUP[DOWN.ordinal()] = 6;
	}

	private EnumFacing rotate(EnumFacing facing, EnumFacing axisFacing) {
        Axis axis = axisFacing.getAxis();
        AxisDirection axisDir = axisFacing.getAxisDirection();

        if (axisDir == AxisDirection.POSITIVE) {
            return facing.rotateAround(axis);
        }

        if (facing.getAxis() != axis) {
            switch (axis) {
            case X:
                // I did some manual testing and this is what worked...I don't get it either
                switch (FACING_LOOKUP[facing.ordinal()]) {
                case 1:
                    return NORTH;
                case 2:
                case 4:
                default:
                    return facing; // Invalid but ignored
                case 3:
                    return SOUTH;
                case 5:
                    return SOUTH;
                case 6:
                    return NORTH;
                }
            case Y:
                return facing.rotateYCCW();
            case Z:
                switch (FACING_LOOKUP[facing.ordinal()]) {
                case 2:
                    return EAST;
                case 3:
                default:
                    return facing; // invalid but ignored
                case 4:
                    return WEST;
                case 5:
                    return DOWN;
                case 6:
                    return UP;
                }
            }
        }

        return facing;
	}
}