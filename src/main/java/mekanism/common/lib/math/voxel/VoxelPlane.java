package mekanism.common.lib.math.voxel;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import mekanism.common.lib.multiblock.Structure.Axis;
import net.minecraft.util.math.BlockPos;

public class VoxelPlane {

    private final Axis axis;
    private int minCol, maxCol;
    private int minRow, maxRow;
    private int size;
    private boolean hasController;

    private final Set<BlockPos> outsideSet = new HashSet<>();

    public VoxelPlane(Axis axis, BlockPos pos, boolean controller) {
        this.axis = axis;
        if (controller) {
            size = 1;
            minCol = maxCol = axis.horizontal().getCoord(pos);
            minRow = maxRow = axis.vertical().getCoord(pos);
            hasController = true;
        } else {
            outsideSet.add(pos);
        }
    }

    public boolean isFull() {
        return size > 0 && getMissing() == 0;
    }

    public int getMissing() {
        return (length() * height()) - size;
    }

    public int length() {
        return hasController ? (maxCol - minCol) + 1 : 0;
    }

    public int height() {
        return hasController ? (maxRow - minRow) + 1 : 0;
    }

    public boolean hasController() {
        return hasController;
    }

    public void merge(VoxelPlane other) {
        outsideSet.addAll(other.outsideSet);
        if (other.hasController) {
            size += other.size;
            if (!hasController) {
                minCol = other.minCol;
                maxCol = other.maxCol;
                minRow = other.minRow;
                maxRow = other.maxRow;
            } else {
                minCol = Math.min(minCol, other.minCol);
                maxCol = Math.max(maxCol, other.maxCol);
                minRow = Math.min(minRow, other.minRow);
                maxRow = Math.max(maxRow, other.maxRow);
            }
            hasController = true;
        }
        outsideSet.removeIf(pos -> {
            if (!isOutside(pos)) {
                size++;
                return true;
            }
            return false;
        });
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

    public boolean isOutside(BlockPos pos) {
        int col = axis.horizontal().getCoord(pos), row = axis.vertical().getCoord(pos);
        return col < minCol || col > maxCol || row < minRow || row > maxRow;
    }

    @Override
    public String toString() {
        return "Plane(full=" + isFull() + ",size=" + size() + ",controller=" + hasController + ",bounds=" + Arrays.asList(minCol, minRow, maxCol, maxRow).toString() + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof VoxelPlane)) {
            return false;
        }
        VoxelPlane other = (VoxelPlane) obj;
        return size == other.size && minCol == other.minCol && maxCol == other.maxCol && minRow == other.minRow && maxRow == other.maxRow;
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + size;
        result = 31 * result + minCol;
        result = 31 * result + maxCol;
        result = 31 * result + minRow;
        result = 31 * result + maxRow;
        return result;
    }
}
