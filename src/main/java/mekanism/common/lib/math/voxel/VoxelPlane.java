package mekanism.common.lib.math.voxel;

import java.util.Arrays;
import mekanism.common.lib.multiblock.Structure.Axis;
import net.minecraft.util.math.BlockPos;

public class VoxelPlane {

    private Axis axis;
    private int minCol, maxCol;
    private int minRow, maxRow;
    private int size;

    public VoxelPlane(Axis axis, BlockPos pos) {
        this.axis = axis;
        size = 1;
        minCol = maxCol = axis.horizontal().getCoord(pos);
        minRow = maxRow = axis.vertical().getCoord(pos);
    }

    public boolean isFull() {
        return size > 0 && getMissing() == 0;
    }

    public int getMissing() {
        int length = maxCol - minCol + 1, height = maxRow - minRow + 1;
        return (length * height) - size;
    }

    public void merge(VoxelPlane other) {
        size += other.size;
        minCol = Math.min(minCol, other.minCol);
        maxCol = Math.max(maxCol, other.maxCol);
        minRow = Math.min(minRow, other.minRow);
        maxRow = Math.max(maxRow, other.maxRow);
    }

    public Axis getAxis() {
        return axis;
    }

    public int size() {
        return size;
    }

    public int getMinRow() {
        return minRow;
    }

    public int getMaxRow() {
        return maxRow;
    }

    public int getMinCol() {
        return minCol;
    }

    public int getMaxCol() {
        return maxCol;
    }

    @Override
    public String toString() {
        return "Plane(full=" + isFull() + ",size=" + size() + ",bounds=" + Arrays.asList(minCol, minRow, maxCol, maxRow).toString() + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof VoxelPlane)) {
            return false;
        }
        VoxelPlane other = (VoxelPlane) obj;
        return size == other.size && minCol == other.minCol && maxCol == other.maxCol && minRow == other.minRow && maxRow == other.maxRow;
    }
}
