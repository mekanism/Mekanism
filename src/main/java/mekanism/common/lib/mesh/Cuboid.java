package mekanism.common.lib.mesh;

import mekanism.common.lib.mesh.Structure.BlockPosBuilder;
import net.minecraft.util.math.BlockPos;

public class Cuboid {

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
}
