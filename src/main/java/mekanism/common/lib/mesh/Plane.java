package mekanism.common.lib.mesh;

import java.util.Arrays;
import mekanism.common.lib.mesh.Structure.Axis;
import net.minecraft.util.math.BlockPos;

public class Plane {

    private Axis axis;
    private int minCol, maxCol;
    private int minRow, maxRow;
    private int size;

    public Plane(Axis axis, BlockPos pos) {
        this.axis = axis;
        size = 1;
        minCol = maxCol = axis.horizontal().getCoord(pos);
        minRow = maxRow = axis.vertical().getCoord(pos);
    }

    public boolean isFull() {
        int length = maxCol - minCol + 1, height = maxRow - minRow + 1;
        return size > 0 && length * height == size;
    }

    public void merge(Plane other) {
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
        if (!(obj instanceof Plane)) {
            return false;
        }
        Plane other = (Plane) obj;
        return size == other.size && minCol == other.minCol && maxCol == other.maxCol && minRow == other.minRow && maxRow == other.maxRow;
    }
}
