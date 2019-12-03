package mekanism.common.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.UnaryOperator;
import mekanism.common.Mekanism;
import net.minecraft.util.Direction;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;

//TODO: Search for instances of this comment "//TODO: VoxelShapes"
public final class VoxelShapeUtils {

    private static final Vec3d fromOrigin = new Vec3d(-0.5, -0.5, -0.5);

    /**
     * Prints out a set of strings that make copy pasting easier, for converting a Model to a voxel shape
     */
    public static void printPieces(String name, double x1, double y1, double z1, double x2, double y2, double z2, double rotX, double rotY, double rotZ) {
        //Transform from mekanism model: (8, 24, 8, 8, 24, 8) - (box + (rotationPoint, rotationPoint))
        double nx1 = 8 - (x1 + rotX);
        double ny1 = 24 - (y1 + rotY);
        double nz1 = 8 - (z1 + rotZ);
        double nx2 = 8 - (x2 + rotX);
        double ny2 = 24 - (y2 + rotY);
        double nz2 = 8 - (z2 + rotZ);
        Mekanism.logger.info("makeCuboidShape(" + Math.min(nx1, nx2) + ", " + Math.min(ny1, ny2) + ", " + Math.min(nz1, nz2) + ", " +
                             Math.max(nx1, nx2) + ", " + Math.max(ny1, ny2) + ", " + Math.max(nz1, nz2) + "),//" + name);
    }

    /**
     * Prints out a set of strings that make copy pasting easier, for simplifying a voxel shape
     */
    public static void printSimplified(String name, VoxelShape shape) {
        Mekanism.logger.info("Simplified: " + name);
        shape.toBoundingBoxList().forEach(box -> {
            double nx1 = box.minX * 16;
            double ny1 = box.minY * 16;
            double nz1 = box.minZ * 16;
            double nx2 = box.maxX * 16;
            double ny2 = box.maxY * 16;
            double nz2 = box.maxZ * 16;
            Mekanism.logger.info("makeCuboidShape(" + Math.min(nx1, nx2) + ", " + Math.min(ny1, ny2) + ", " + Math.min(nz1, nz2) + ", " +
                                 Math.max(nx1, nx2) + ", " + Math.max(ny1, ny2) + ", " + Math.max(nz1, nz2) + "),");
        });
    }

    /**
     * Rotates an {@link AxisAlignedBB} to a specific side, similar to how the block states rotate models.
     *
     * @param box  The {@link AxisAlignedBB} to rotate
     * @param side The side to rotate it to.
     *
     * @return The rotated {@link AxisAlignedBB}
     */
    public static AxisAlignedBB rotate(AxisAlignedBB box, Direction side) {
        switch (side) {
            case DOWN:
                return box;
            case UP:
                return new AxisAlignedBB(box.minX, -box.minY, -box.minZ, box.maxX, -box.maxY, -box.maxZ);
            case NORTH:
                return new AxisAlignedBB(box.minX, -box.minZ, box.minY, box.maxX, -box.maxZ, box.maxY);
            case SOUTH:
                return new AxisAlignedBB(box.minX, box.minZ, -box.minY, box.maxX, box.maxZ, -box.maxY);
            case WEST:
                return new AxisAlignedBB(box.minY, -box.minZ, box.minX, box.maxY, -box.maxZ, box.maxX);
            case EAST:
                return new AxisAlignedBB(-box.minY, box.minZ, box.minX, -box.maxY, box.maxZ, box.maxX);
        }
        return box;
    }

    /**
     * Rotates an {@link AxisAlignedBB} to a according to a specific rotation.
     *
     * @param box      The {@link AxisAlignedBB} to rotate
     * @param rotation The rotation we are performing.
     *
     * @return The rotated {@link AxisAlignedBB}
     */
    public static AxisAlignedBB rotate(AxisAlignedBB box, Rotation rotation) {
        switch (rotation) {
            case NONE:
                return box;
            case CLOCKWISE_90:
                return new AxisAlignedBB(-box.minZ, box.minY, box.minX, -box.maxZ, box.maxY, box.maxX);
            case CLOCKWISE_180:
                return new AxisAlignedBB(-box.minX, box.minY, -box.minZ, -box.maxX, box.maxY, -box.maxZ);
            case COUNTERCLOCKWISE_90:
                return new AxisAlignedBB(box.minZ, box.minY, -box.minX, box.maxZ, box.maxY, -box.maxX);
        }
        return box;
    }

    /**
     * Rotates an {@link AxisAlignedBB} to a specific side horizontally. This is a default most common rotation setup as to {@link #rotate(AxisAlignedBB, Rotation)}
     *
     * @param box  The {@link AxisAlignedBB} to rotate
     * @param side The side to rotate it to.
     *
     * @return The rotated {@link AxisAlignedBB}
     */
    public static AxisAlignedBB rotateHorizontal(AxisAlignedBB box, Direction side) {
        switch (side) {
            case NORTH:
                return rotate(box, Rotation.NONE);
            case SOUTH:
                return rotate(box, Rotation.CLOCKWISE_180);
            case WEST:
                return rotate(box, Rotation.COUNTERCLOCKWISE_90);
            case EAST:
                return rotate(box, Rotation.CLOCKWISE_90);
        }
        return box;
    }

    /**
     * Rotates a {@link VoxelShape} to a specific side, similar to how the block states rotate models.
     *
     * @param shape The {@link VoxelShape} to rotate
     * @param side  The side to rotate it to.
     *
     * @return The rotated {@link VoxelShape}
     */
    public static VoxelShape rotate(VoxelShape shape, Direction side) {
        return rotate(shape, box -> rotate(box, side));
    }

    /**
     * Rotates a {@link VoxelShape} to a according to a specific rotation.
     *
     * @param shape    The {@link VoxelShape} to rotate
     * @param rotation The rotation we are performing.
     *
     * @return The rotated {@link VoxelShape}
     */
    public static VoxelShape rotate(VoxelShape shape, Rotation rotation) {
        return rotate(shape, box -> rotate(box, rotation));
    }

    /**
     * Rotates a {@link VoxelShape} to a specific side horizontally. This is a default most common rotation setup as to {@link #rotate(VoxelShape, Rotation)}
     *
     * @param shape The {@link VoxelShape} to rotate
     * @param side  The side to rotate it to.
     *
     * @return The rotated {@link VoxelShape}
     */
    public static VoxelShape rotateHorizontal(VoxelShape shape, Direction side) {
        return rotate(shape, box -> rotateHorizontal(box, side));
    }

    /**
     * Rotates a {@link VoxelShape} using a specific transformation function for each {@link AxisAlignedBB} in the {@link VoxelShape}.
     *
     * @param shape          The {@link VoxelShape} to rotate
     * @param rotateFunction The transformation function to apply to each {@link AxisAlignedBB} in the {@link VoxelShape}.
     *
     * @return The rotated {@link VoxelShape}
     */
    public static VoxelShape rotate(VoxelShape shape, UnaryOperator<AxisAlignedBB> rotateFunction) {
        List<VoxelShape> rotatedPieces = new ArrayList<>();
        //Explode the voxel shape into bounding boxes
        List<AxisAlignedBB> sourceBoundingBoxes = shape.toBoundingBoxList();
        //Rotate them and convert them each back into a voxel shape
        for (AxisAlignedBB sourceBoundingBox : sourceBoundingBoxes) {
            //Make the bounding box be centered around the middle, and then move it back after rotating
            rotatedPieces.add(VoxelShapes.create(rotateFunction.apply(sourceBoundingBox.offset(fromOrigin.x, fromOrigin.y, fromOrigin.z))
                  .offset(-fromOrigin.x, -fromOrigin.z, -fromOrigin.z)));
        }
        //return the recombined rotated voxel shape
        return combine(rotatedPieces);
    }

    /**
     * Used for mass combining shapes
     *
     * @param shapes The list of {@link VoxelShape}s to include
     *
     * @return A {@link VoxelShape} including everything that is part of any of the input shapes.
     */
    public static VoxelShape combine(VoxelShape... shapes) {
        return batchCombine(VoxelShapes.empty(), IBooleanFunction.OR, shapes);
    }

    /**
     * Used for mass combining shapes
     *
     * @param shapes The collection of {@link VoxelShape}s to include
     *
     * @return A {@link VoxelShape} including everything that is part of any of the input shapes.
     */
    public static VoxelShape combine(Collection<VoxelShape> shapes) {
        return batchCombine(VoxelShapes.empty(), IBooleanFunction.OR, shapes);
    }

    /**
     * Used for cutting shapes out of a full cube
     *
     * @param shapes The list of {@link VoxelShape}s to cut out
     *
     * @return A {@link VoxelShape} including everything that is not part of any of the input shapes.
     */
    public static VoxelShape exclude(VoxelShape... shapes) {
        return batchCombine(VoxelShapes.fullCube(), IBooleanFunction.ONLY_FIRST, shapes);
    }

    /**
     * Used for mass combining shapes using a specific {@link IBooleanFunction} and a given start shape.
     *
     * @param initial  The {@link VoxelShape} to start with
     * @param function The {@link IBooleanFunction} to perform
     * @param shapes   The collection of {@link VoxelShape}s to include
     *
     * @return A {@link VoxelShape} based on the input parameters.
     */
    public static VoxelShape batchCombine(VoxelShape initial, IBooleanFunction function, Collection<VoxelShape> shapes) {
        VoxelShape combinedShape = initial;
        for (VoxelShape shape : shapes) {
            combinedShape = VoxelShapes.combineAndSimplify(combinedShape, shape, function);
        }
        return combinedShape;
    }

    /**
     * Used for mass combining shapes using a specific {@link IBooleanFunction} and a given start shape.
     *
     * @param initial  The {@link VoxelShape} to start with
     * @param function The {@link IBooleanFunction} to perform
     * @param shapes   The list of {@link VoxelShape}s to include
     *
     * @return A {@link VoxelShape} based on the input parameters.
     */
    public static VoxelShape batchCombine(VoxelShape initial, IBooleanFunction function, VoxelShape... shapes) {
        VoxelShape combinedShape = initial;
        for (VoxelShape shape : shapes) {
            combinedShape = VoxelShapes.combineAndSimplify(combinedShape, shape, function);
        }
        return combinedShape;
    }
}