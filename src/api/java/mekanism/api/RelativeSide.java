package mekanism.api;

import javax.annotation.Nonnull;
import net.minecraft.util.Direction;

public enum RelativeSide {
    FRONT,
    LEFT,
    RIGHT,
    BACK,
    TOP,
    BOTTOM;

    /**
     * Gets the {@link RelativeSide} based on a side, and the facing direction of a block.
     * @param facing The direction the block is facing.
     * @param side The side of the block we want to know what {@link RelativeSide} it is.
     * @return the {@link RelativeSide} based on a side, and the facing direction of a block.
     *
     * @apiNote The calculations for what side is what when facing upwards or downwards, is done as if it was facing NORTH and rotated around the X-axis
     */
    public static RelativeSide fromDirections(@Nonnull Direction facing, @Nonnull Direction side) {
        //TODO: See if this if statement block can be cleaned up given it looks somewhat messy
        if (side == facing) {
            return FRONT;
        } else if (side == facing.getOpposite()) {
            return BACK;
        } else if (facing == Direction.DOWN) {
            if (side == Direction.NORTH) {
                return TOP;
            } else if (side == Direction.SOUTH) {
                return BOTTOM;
            } else if (side == Direction.WEST) {
                return RIGHT;
            } else if (side == Direction.EAST) {
                return LEFT;
            }
        } else if (facing == Direction.UP) {
            if (side == Direction.NORTH) {
                return BOTTOM;
            } else if (side == Direction.SOUTH) {
                return TOP;
            } else if (side == Direction.WEST) {
                return RIGHT;
            } else if (side == Direction.EAST) {
                return LEFT;
            }
        } else if (side == Direction.DOWN) {
            return BOTTOM;
        } else if (side == Direction.UP) {
            return TOP;
        } else if (side == facing.rotateYCCW()) {
            return RIGHT;
        } else if (side == facing.rotateY()) {
            return LEFT;
        }
        //Fall back to front, should never get here
        return FRONT;
    }
}