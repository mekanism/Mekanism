package mekanism.common.lib.multiblock;

import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import mekanism.common.lib.math.voxel.VoxelCuboid;
import mekanism.common.lib.math.voxel.VoxelCuboid.CuboidBuilder;
import mekanism.common.lib.math.voxel.VoxelCuboid.CuboidSide;
import mekanism.common.lib.math.voxel.VoxelCuboid.CuboidSide.Face;
import mekanism.common.lib.math.voxel.VoxelPlane;
import mekanism.common.lib.multiblock.Structure.Axis;

public class StructureHelper {

    private StructureHelper() {
    }

    /**
     * Fetch a cuboid with all 6 sides present. Quicker than using the below algorithm with all sides.
     *
     * @param structure structure to check
     * @param minBounds minimum size of the cuboid
     * @param maxBounds maximum size of the cuboid
     *
     * @return found cuboid, or null if it doesn't exist
     */
    public static VoxelCuboid fetchCuboid(Structure structure, VoxelCuboid minBounds, VoxelCuboid maxBounds) {
        VoxelCuboid prev = null;
        for (Axis axis : Axis.AXES) {
            NavigableMap<Integer, VoxelPlane> map = structure.getMajorAxisMap(axis);
            Map.Entry<Integer, VoxelPlane> first = map.firstEntry(), last = map.lastEntry();
            if (first == null || !first.getValue().equals(last.getValue()) || !first.getValue().isFull()) {
                return null;
            }
            VoxelCuboid cuboid = VoxelCuboid.from(first.getValue(), last.getValue(), first.getKey(), last.getKey());
            // if this is the first axial cuboid check, make sure we have the correct bounds
            if (prev == null && (!cuboid.greaterOrEqual(minBounds) || !maxBounds.greaterOrEqual(cuboid))) {
                return null;
            }
            // if this isn't the first axial cuboid check, make sure the cuboids match
            if (prev != null && !prev.equals(cuboid)) {
                return null;
            }
            prev = cuboid;
        }
        return prev;
    }

    /**
     * Fetch a cuboid with a defined amount of sides. At least two sides should be provided; otherwise it's impossible to discern the overall dimensions about the
     * cuboid.
     *
     * @param structure structure to check
     * @param minBounds minimum size of the cuboid
     * @param maxBounds maximum size of the cuboid
     * @param sides     sides to check
     * @param tolerance how many missing blocks are tolerated in the completed structure (will double count edges & triple count corners)
     *
     * @return found cuboid, or null if it doesn't exist
     */
    public static VoxelCuboid fetchCuboid(Structure structure, VoxelCuboid minBounds, VoxelCuboid maxBounds, Set<CuboidSide> sides, int tolerance) {
        // make sure we have enough sides to create cuboidal dimensions
        if (sides.size() < 2) {
            return null;
        }
        int missing = 0;
        CuboidBuilder builder = new CuboidBuilder();
        for (CuboidSide side : sides) {
            Axis axis = side.getAxis(), horizontal = axis.horizontal(), vertical = axis.vertical();
            NavigableMap<Integer, VoxelPlane> map = structure.getMajorAxisMap(axis);
            Map.Entry<Integer, VoxelPlane> entry = side.getFace().isPositive() ? map.lastEntry() : map.firstEntry();
            // fail fast if the plane doesn't exist
            if (entry == null) {
                return null;
            }
            VoxelPlane plane = entry.getValue();
            // handle missing blocks based on tolerance value
            missing += plane.getMissing();
            if (missing > tolerance) {
                return null;
            }
            // set bounds from dimension of plane's axis
            builder.set(side, entry.getKey());
            // update cuboidal dimensions from each corner of the plane
            if (!builder.trySet(CuboidSide.get(Face.NEGATIVE, horizontal), plane.getMinCol()) ||
                !builder.trySet(CuboidSide.get(Face.POSITIVE, horizontal), plane.getMaxCol()) ||
                !builder.trySet(CuboidSide.get(Face.NEGATIVE, vertical), plane.getMinRow()) ||
                !builder.trySet(CuboidSide.get(Face.POSITIVE, vertical), plane.getMaxRow())) {
                return null;
            }
        }
        VoxelCuboid ret = builder.build();
        // make sure the cuboid has the correct bounds
        if (!ret.greaterOrEqual(minBounds) || !maxBounds.greaterOrEqual(ret)) {
            return null;
        }
        return ret;
    }
}