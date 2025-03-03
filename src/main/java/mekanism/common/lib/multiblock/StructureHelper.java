package mekanism.common.lib.multiblock;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectSortedMap;
import it.unimi.dsi.fastutil.objects.ObjectBidirectionalIterator;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectSortedSet;
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
            ObjectSortedSet<Int2ObjectMap.Entry<VoxelPlane>> majorAxisMap = structure.getMajorAxisMap(axis).int2ObjectEntrySet();
            Int2ObjectMap.Entry<VoxelPlane> firstMajor = majorAxisMap.first(), lastMajor = majorAxisMap.last();
            if (firstMajor == null || !firstMajor.getValue().equals(lastMajor.getValue()) || !firstMajor.getValue().isFull()) {
                //If there is no major plane, or the two parallel planes are mismatched in size,
                // or if the plane is missing pieces then fail
                return null;
            }
            VoxelCuboid cuboid = VoxelCuboid.from(firstMajor.getValue(), lastMajor.getValue(), firstMajor.getIntKey(), lastMajor.getIntKey());
            // if this is the first axial cuboid check, make sure we have the correct bounds
            if (prev == null && (!cuboid.greaterOrEqual(minBounds) || !maxBounds.greaterOrEqual(cuboid))) {
                return null;
            }
            // if this isn't the first axial cuboid check, make sure the cuboids match
            if (prev != null && !prev.equals(cuboid)) {
                return null;
            }
            // check to make sure that we don't have any framed minor planes sticking out of our cuboid
            Int2ObjectSortedMap<VoxelPlane> minorAxisMap = structure.getMinorAxisMap(axis);
            if (!minorAxisMap.isEmpty()) {
                if (hasOutOfBoundsNegativeMinor(minorAxisMap, firstMajor.getIntKey()) || hasOutOfBoundsPositiveMinor(minorAxisMap, lastMajor.getIntKey())) {
                    return null;
                }
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
            ObjectSortedSet<Int2ObjectMap.Entry<VoxelPlane>> majorAxisMap = structure.getMajorAxisMap(axis).int2ObjectEntrySet();
            Int2ObjectMap.Entry<VoxelPlane> majorEntry = side.getFace().isPositive() ? majorAxisMap.last() : majorAxisMap.first();
            // fail fast if the plane doesn't exist
            if (majorEntry == null) {
                return null;
            }
            VoxelPlane plane = majorEntry.getValue();
            // handle missing blocks based on tolerance value
            missing += plane.getMissing();
            if (missing > tolerance) {
                return null;
            }
            int majorKey = majorEntry.getIntKey();
            // set bounds from dimension of plane's axis
            builder.set(side, majorKey);
            // update cuboidal dimensions from each corner of the plane
            if (!builder.trySet(CuboidSide.get(Face.NEGATIVE, horizontal), plane.getMinCol()) ||
                !builder.trySet(CuboidSide.get(Face.POSITIVE, horizontal), plane.getMaxCol()) ||
                !builder.trySet(CuboidSide.get(Face.NEGATIVE, vertical), plane.getMinRow()) ||
                !builder.trySet(CuboidSide.get(Face.POSITIVE, vertical), plane.getMaxRow())) {
                return null;
            }
            // check to make sure that we don't have any framed minor planes sticking out of our plane
            Int2ObjectSortedMap<VoxelPlane> minorAxisMap = structure.getMinorAxisMap(axis);
            if (!minorAxisMap.isEmpty()) {
                if (side.getFace().isPositive()) {
                    if (hasOutOfBoundsPositiveMinor(minorAxisMap, majorKey)) {
                        return null;
                    }
                } else if (hasOutOfBoundsNegativeMinor(minorAxisMap, majorKey)) {
                    return null;
                }
            }
        }
        VoxelCuboid ret = builder.build();
        // make sure the cuboid has the correct bounds
        if (!ret.greaterOrEqual(minBounds) || !maxBounds.greaterOrEqual(ret)) {
            return null;
        }
        return ret;
    }

    /**
     * Checks if any of the minor planes that have frames and are not purely structural are sticking out past the major plane in the positive cuboid direction (Top,
     * South, East).
     *
     * @param minorAxisMap Map of minor planes.
     * @param majorKey     Position of major plane.
     *
     * @return {@code true} if there are minor planes sticking out.
     */
    private static boolean hasOutOfBoundsPositiveMinor(Int2ObjectSortedMap<VoxelPlane> minorAxisMap, int majorKey) {
        ObjectSortedSet<Int2ObjectMap.Entry<VoxelPlane>> entries = minorAxisMap.int2ObjectEntrySet();
        ObjectBidirectionalIterator<Int2ObjectMap.Entry<VoxelPlane>> iterator = entries.iterator(entries.last());
        while (iterator.hasPrevious()) {
            Int2ObjectMap.Entry<VoxelPlane> minorEntry = iterator.previous();
            int minorKey = minorEntry.getIntKey();
            if (minorKey <= majorKey) {
                //If our outer minor plane is in the bounds of our major plane
                // then just exit as the other minor planes will be as well
                break;
            } else if (minorEntry.getValue().hasFrame()) {
                //Otherwise, if it isn't in the bounds, and it has a frame, fail out
                return true;
            }
            //If we don't have a frame and are out of bounds, see if we have any minor entries that
            // are "smaller" that may be invalid
        }
        return false;
    }

    /**
     * Checks if any of the minor planes that have frames and are not purely structural are sticking out past the major plane in the negative cuboid direction (Bottom,
     * North, West).
     *
     * @param minorAxisMap Map of minor planes.
     * @param majorKey     Position of major plane.
     *
     * @return {@code true} if there are minor planes sticking out.
     */
    private static boolean hasOutOfBoundsNegativeMinor(Int2ObjectSortedMap<VoxelPlane> minorAxisMap, int majorKey) {
        for (ObjectIterator<Int2ObjectMap.Entry<VoxelPlane>> iterator = Int2ObjectMaps.fastIterator(minorAxisMap); iterator.hasNext(); ) {
            Int2ObjectMap.Entry<VoxelPlane> minorEntry = iterator.next();
            int minorKey = minorEntry.getIntKey();
            if (minorKey >= majorKey) {
                //If our outer minor plane is in the bounds of our major plane
                // then just exit as the other minor planes will be as well
                break;
            } else if (minorEntry.getValue().hasFrame()) {
                //Otherwise, if it isn't in the bounds, and it has a frame, fail out
                return true;
            }
            //If we don't have a frame and are out of bounds, see if we have any minor entries that
            // are "smaller" that may be invalid
        }
        return false;
    }
}