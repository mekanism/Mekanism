package mekanism.common.lib.math.voxel;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import mekanism.common.lib.multiblock.Structure.Axis;
import net.minecraft.core.BlockPos;

public class VoxelPlane {

    private final Axis axis;
    private int minCol, maxCol;
    private int minRow, maxRow;
    private int size;
    private boolean hasFrame;

    private final Set<BlockPos> outsideSet = new HashSet<>();

    public VoxelPlane(Axis axis, BlockPos pos, boolean frame) {
        this.axis = axis;
        if (frame) {
            size = 1;
            minCol = maxCol = axis.horizontal().getCoord(pos);
            minRow = maxRow = axis.vertical().getCoord(pos);
            hasFrame = true;
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
        return hasFrame ? (maxCol - minCol) + 1 : 0;
    }

    public int height() {
        return hasFrame ? (maxRow - minRow) + 1 : 0;
    }

    public boolean hasFrame() {
        return hasFrame;
    }

    public void merge(VoxelPlane other) {
        //Combine all blocks in the plane that are outside the frames
        outsideSet.addAll(other.outsideSet);
        if (other.hasFrame) {
            //If the other VoxelPlane has a frame around it
            // increase our size by how many it has in the frame
            size += other.size;
            // and update the bounds of our plane
            if (hasFrame) {
                minCol = Math.min(minCol, other.minCol);
                maxCol = Math.max(maxCol, other.maxCol);
                minRow = Math.min(minRow, other.minRow);
                maxRow = Math.max(maxRow, other.maxRow);
            } else {
                minCol = other.minCol;
                maxCol = other.maxCol;
                minRow = other.minRow;
                maxRow = other.maxRow;
                hasFrame = true;
            }
        }
        if (hasFrame) {
            //Afterwards if we have a frame, go through all the blocks that are outside
            for (Iterator<BlockPos> iterator = outsideSet.iterator(); iterator.hasNext(); ) {
                if (!isOutside(iterator.next())) {
                    // and if they are inside our frame, add them to our plane's size and remove them
                    // from the positions that are outside of frame but in the plane
                    size++;
                    iterator.remove();
                }
            }
        }
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
        return "Plane(full=" + isFull() + ", size=" + size() + ", frame=" + hasFrame + ", bounds=" + List.of(minCol, minRow, maxCol, maxRow) + ")";
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof VoxelPlane other && size == other.size && minCol == other.minCol && maxCol == other.maxCol && minRow == other.minRow && maxRow == other.maxRow;
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