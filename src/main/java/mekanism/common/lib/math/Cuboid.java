package mekanism.common.lib.math;

import mekanism.common.lib.multiblock.Structure.Axis;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

public class Cuboid implements IShape {

    private BlockPos minPos;
    private BlockPos maxPos;

    public Cuboid(BlockPos minPos, BlockPos maxPos) {
        this.minPos = minPos;
        this.maxPos = maxPos;
    }

    public Cuboid(int length, int height, int width) {
        this(new BlockPos(0, 0, 0), new BlockPos(length - 1, height - 1, width - 1));
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

    public Direction getSide(BlockPos pos) {
        if (pos.getX() == minPos.getX()) {
            return Direction.WEST;
        } else if (pos.getX() == maxPos.getX()) {
            return Direction.EAST;
        } else if (pos.getY() == minPos.getY()) {
            return Direction.DOWN;
        } else if (pos.getY() == maxPos.getY()) {
            return Direction.UP;
        } else if (pos.getZ() == minPos.getZ()) {
            return Direction.NORTH;
        } else if (pos.getZ() == maxPos.getZ()) {
            return Direction.SOUTH;
        }
        return null;
    }

    public boolean isOnSide(BlockPos pos) {
        return getMatches(pos) >= 1;
    }

    public boolean isOnEdge(BlockPos pos) {
        return getMatches(pos) >= 2;
    }

    public boolean isOnCorner(BlockPos pos) {
        return getMatches(pos) >= 3;
    }

    public int getMatches(BlockPos pos) {
        int matches = 0;
        if (pos.getX() == minPos.getX())
            matches++;
        if (pos.getX() == maxPos.getX())
            matches++;
        if (pos.getY() == minPos.getY())
            matches++;
        if (pos.getY() == maxPos.getY())
            matches++;
        if (pos.getZ() == minPos.getZ())
            matches++;
        if (pos.getZ() == maxPos.getZ())
            matches++;
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

    public boolean greaterOrEqual(Cuboid other) {
        return length() >= other.length() && width() >= other.width() && height() >= other.height();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Cuboid)) {
            return false;
        }
        Cuboid other = (Cuboid) obj;
        return minPos.equals(other.minPos) && maxPos.equals(other.maxPos);
    }

    public static Cuboid from(Plane p1, Plane p2, int p1Pos, int p2Pos) {
        BlockPosBuilder min = new BlockPosBuilder();
        BlockPosBuilder max = new BlockPosBuilder();
        p1.getAxis().set(min, p1Pos);
        p2.getAxis().set(max, p2Pos);
        p1.getAxis().horizontal().set(min, p1.getMinCol());
        p1.getAxis().horizontal().set(max, p1.getMaxCol());
        p1.getAxis().vertical().set(min, p1.getMinRow());
        p1.getAxis().vertical().set(max, p1.getMaxRow());
        return new Cuboid(min.build(), max.build());
    }

    @Override
    public String toString() {
        return "Cuboid(start=" + minPos + ",bounds=(" + length() + "," + height() + "," + width() + "))";
    }

    public enum CuboidRelative {
        INSIDE,
        OUTSIDE,
        WALLS;
    }

    public enum CuboidSide {
        BOTTOM(Axis.Y, Face.NEGATIVE),
        TOP(Axis.Y, Face.POSITIVE),
        NORTH(Axis.Z, Face.NEGATIVE),
        SOUTH(Axis.Z, Face.POSITIVE),
        WEST(Axis.X, Face.NEGATIVE),
        EAST(Axis.X, Face.POSITIVE);

        public static final CuboidSide[] SIDES = values();

        private static final CuboidSide[][] ORDERED = new CuboidSide[][] {{WEST, BOTTOM, NORTH}, {EAST, TOP, SOUTH}};
        private static final CuboidSide[] OPPOSITES = new CuboidSide[] {TOP, BOTTOM, SOUTH, NORTH, EAST, WEST};

        private Axis axis;
        private Face face;

        private CuboidSide(Axis axis, Face face) {
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

        private BlockPosBuilder[] bounds = new BlockPosBuilder[] {new BlockPosBuilder(), new BlockPosBuilder()};

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

        public Cuboid build() {
            return new Cuboid(bounds[0].build(), bounds[1].build());
        }
    }
}
