package mekanism.common.lib.math.voxel;

import mekanism.common.lib.multiblock.Structure.Axis;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;

public class VoxelCuboid implements IShape {

    private BlockPos minPos;
    private BlockPos maxPos;
    private AABB asAABB;

    public VoxelCuboid(BlockPos minPos, BlockPos maxPos) {
        this.minPos = minPos;
        this.maxPos = maxPos;
        this.asAABB = AABB.encapsulatingFullBlocks(minPos, maxPos);
    }

    public VoxelCuboid(int length, int height, int width) {
        this(BlockPos.ZERO, new BlockPos(length - 1, height - 1, width - 1));
    }

    public int length() {
        return maxPos.getX() - minPos.getX() + 1;
    }

    public int width() {
        return maxPos.getZ() - minPos.getZ() + 1;
    }

    public int height() {
        return maxPos.getY() - minPos.getY() + 1;
    }

    public BlockPos getMinPos() {
        return minPos;
    }

    public BlockPos getMaxPos() {
        return maxPos;
    }

    public void setMinPos(BlockPos minPos) {
        this.minPos = minPos;
        this.asAABB = AABB.encapsulatingFullBlocks(minPos, maxPos);
    }

    public void setMaxPos(BlockPos maxPos) {
        this.maxPos = maxPos;
        this.asAABB = AABB.encapsulatingFullBlocks(minPos, maxPos);
    }

    public BlockPos getCenter() {
        return new BlockPos((minPos.getX() + maxPos.getX()) / 2,
              (minPos.getY() + maxPos.getY()) / 2,
              (minPos.getZ() + maxPos.getZ()) / 2);
    }

    public Direction getSide(BlockPos pos) {
        if (pos.getX() == minPos.getX()) {
            return Direction.WEST;
        } else if (pos.getX() == maxPos.getX()) {
            return Direction.EAST;
        } else if (pos.getZ() == minPos.getZ()) {
            return Direction.NORTH;
        } else if (pos.getZ() == maxPos.getZ()) {
            return Direction.SOUTH;
        } else if (pos.getY() == minPos.getY()) {
            return Direction.DOWN;
        } else if (pos.getY() == maxPos.getY()) {
            return Direction.UP;
        }
        return null;
    }

    public boolean isOnSide(BlockPos pos) {
        return getWallRelative(pos).isWall();
    }

    public boolean isOnEdge(BlockPos pos) {
        return getWallRelative(pos).isOnEdge();
    }

    public boolean isOnCorner(BlockPos pos) {
        return getWallRelative(pos).isOnCorner();
    }

    public WallRelative getWallRelative(BlockPos pos) {
        int matches = getMatches(pos);
        if (matches >= 3) {
            return WallRelative.CORNER;
        } else if (matches == 2) {
            return WallRelative.EDGE;
        } else if (matches == 1) {
            return WallRelative.SIDE;
        }
        return WallRelative.INVALID;
    }

    public int getMatches(BlockPos pos) {
        int matches = 0;
        if (pos.getX() == minPos.getX()) {
            matches++;
        }
        if (pos.getX() == maxPos.getX()) {
            matches++;
        }
        if (pos.getY() == minPos.getY()) {
            matches++;
        }
        if (pos.getY() == maxPos.getY()) {
            matches++;
        }
        if (pos.getZ() == minPos.getZ()) {
            matches++;
        }
        if (pos.getZ() == maxPos.getZ()) {
            matches++;
        }
        return matches;
    }

    public CuboidRelative getRelativeLocation(BlockPos pos) {
        if (pos.getX() > minPos.getX() && pos.getX() < maxPos.getX() &&
            pos.getY() > minPos.getY() && pos.getY() < maxPos.getY() &&
            pos.getZ() > minPos.getZ() && pos.getZ() < maxPos.getZ()) {
            return CuboidRelative.INSIDE;
        } else if (pos.getX() < minPos.getX() || pos.getX() > maxPos.getX() ||
                   pos.getY() < minPos.getY() || pos.getY() > maxPos.getY() ||
                   pos.getZ() < minPos.getZ() || pos.getZ() > maxPos.getZ()) {
            return CuboidRelative.OUTSIDE;
        }
        return CuboidRelative.WALLS;
    }

    public boolean greaterOrEqual(VoxelCuboid other) {
        return length() >= other.length() && width() >= other.width() && height() >= other.height();
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + maxPos.hashCode();
        result = 31 * result + minPos.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof VoxelCuboid other && minPos.equals(other.minPos) && maxPos.equals(other.maxPos);
    }

    public static VoxelCuboid from(VoxelPlane p1, VoxelPlane p2, int p1Pos, int p2Pos) {
        BlockPosBuilder min = new BlockPosBuilder();
        BlockPosBuilder max = new BlockPosBuilder();
        p1.getAxis().set(min, p1Pos);
        p2.getAxis().set(max, p2Pos);
        p1.getAxis().horizontal().set(min, p1.getMinCol());
        p1.getAxis().horizontal().set(max, p1.getMaxCol());
        p1.getAxis().vertical().set(min, p1.getMinRow());
        p1.getAxis().vertical().set(max, p1.getMaxRow());
        return new VoxelCuboid(min.build(), max.build());
    }

    @Override
    public String toString() {
        return "Cuboid(start=" + minPos + ", bounds=(" + length() + "," + height() + "," + width() + "))";
    }

    public AABB asAABB() {
        return asAABB;
    }

    public enum WallRelative {
        SIDE,
        EDGE,
        CORNER,
        INVALID;

        public boolean isWall() {
            return this != INVALID;
        }

        public boolean isOnEdge() {
            return this == EDGE || this == CORNER;
        }

        public boolean isOnCorner() {
            return this == CORNER;
        }
    }

    public enum CuboidRelative {
        INSIDE,
        OUTSIDE,
        WALLS;

        public boolean isWall() {
            return this == WALLS;
        }
    }

    public enum CuboidSide {
        BOTTOM(Axis.Y, Face.NEGATIVE),
        TOP(Axis.Y, Face.POSITIVE),
        NORTH(Axis.Z, Face.NEGATIVE),
        SOUTH(Axis.Z, Face.POSITIVE),
        WEST(Axis.X, Face.NEGATIVE),
        EAST(Axis.X, Face.POSITIVE);

        public static final CuboidSide[] SIDES = values();

        private static final CuboidSide[][] ORDERED = {{WEST, BOTTOM, NORTH}, {EAST, TOP, SOUTH}};
        private static final CuboidSide[] OPPOSITES = {TOP, BOTTOM, SOUTH, NORTH, EAST, WEST};

        private final Axis axis;
        private final Face face;

        CuboidSide(Axis axis, Face face) {
            this.axis = axis;
            this.face = face;
        }

        public Axis getAxis() {
            return axis;
        }

        public Face getFace() {
            return face;
        }

        public CuboidSide flip() {
            return OPPOSITES[ordinal()];
        }

        public static CuboidSide get(Face face, Axis axis) {
            return ORDERED[face.ordinal()][axis.ordinal()];
        }

        public enum Face {
            NEGATIVE,
            POSITIVE;

            public Face getOpposite() {
                return this == POSITIVE ? NEGATIVE : POSITIVE;
            }

            public boolean isPositive() {
                return this == POSITIVE;
            }
        }
    }

    public static class CuboidBuilder {

        private final BlockPosBuilder[] bounds = {new BlockPosBuilder(), new BlockPosBuilder()};

        public boolean isSet(CuboidSide side) {
            return bounds[side.getFace().ordinal()].isSet(side.getAxis());
        }

        public void set(CuboidSide side, int val) {
            bounds[side.getFace().ordinal()].set(side.getAxis(), val);
        }

        public boolean trySet(CuboidSide side, int val) {
            if (isSet(side) && get(side) != val) {
                return false;
            }
            set(side, val);
            return true;
        }

        public int get(CuboidSide side) {
            return bounds[side.getFace().ordinal()].get(side.getAxis());
        }

        public VoxelCuboid build() {
            return new VoxelCuboid(bounds[0].build(), bounds[1].build());
        }
    }
}
