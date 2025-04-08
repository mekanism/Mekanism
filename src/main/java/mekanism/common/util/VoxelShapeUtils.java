package mekanism.common.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.UnaryOperator;
import mekanism.common.Mekanism;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class VoxelShapeUtils {

    private static final Vec3 fromOrigin = new Vec3(-0.5, -0.5, -0.5);

    /**
     * Prints out an easy to copy-paste string representing the cuboid of a shape
     */
    public static void print(double x1, double y1, double z1, double x2, double y2, double z2) {
        Mekanism.logger.info("box({}, {}, {}, {}, {}, {}),", Math.min(x1, x2), Math.min(y1, y2), Math.min(z1, z2),
              Math.max(x1, x2), Math.max(y1, y2), Math.max(z1, z2));
    }

    /**
     * Prints out a set of strings that make copy-pasting easier, for simplifying a voxel shape
     */
    public static void printSimplified(String name, VoxelShape shape) {
        Mekanism.logger.info("Simplified: {}", name);
        for (AABB box : shape.optimize().toAabbs()) {
            print(box.minX * 16, box.minY * 16, box.minZ * 16, box.maxX * 16, box.maxY * 16, box.maxZ * 16);
        }
    }

    /**
     * Rotates an {@link AABB} to a specific side, similar to how the block states rotate models.
     *
     * @param box  The {@link AABB} to rotate
     * @param side The side to rotate it to.
     *
     * @return The rotated {@link AABB}
     */
    public static AABB rotate(AABB box, Direction side) {
        return switch (side) {
            case DOWN -> box;
            case UP -> new AABB(box.minX, -box.minY, -box.minZ, box.maxX, -box.maxY, -box.maxZ);
            case NORTH -> new AABB(box.minX, -box.minZ, box.minY, box.maxX, -box.maxZ, box.maxY);
            case SOUTH -> new AABB(-box.minX, -box.minZ, -box.minY, -box.maxX, -box.maxZ, -box.maxY);
            case WEST -> new AABB(box.minY, -box.minZ, -box.minX, box.maxY, -box.maxZ, -box.maxX);
            case EAST -> new AABB(-box.minY, -box.minZ, box.minX, -box.maxY, -box.maxZ, box.maxX);
        };
    }

    /**
     * Rotates an {@link AABB} according to a specific rotation.
     *
     * @param box      The {@link AABB} to rotate
     * @param rotation The rotation we are performing.
     *
     * @return The rotated {@link AABB}
     */
    public static AABB rotate(AABB box, Rotation rotation) {
        return switch (rotation) {
            case NONE -> box;
            case CLOCKWISE_90 -> new AABB(-box.minZ, box.minY, box.minX, -box.maxZ, box.maxY, box.maxX);
            case CLOCKWISE_180 -> new AABB(-box.minX, box.minY, -box.minZ, -box.maxX, box.maxY, -box.maxZ);
            case COUNTERCLOCKWISE_90 -> new AABB(box.minZ, box.minY, -box.minX, box.maxZ, box.maxY, -box.maxX);
        };
    }

    /**
     * Rotates an {@link AABB} to a specific side horizontally. This is a default most common rotation setup as to {@link #rotate(AABB, Rotation)}
     *
     * @param box  The {@link AABB} to rotate
     * @param side The side to rotate it to.
     *
     * @return The rotated {@link AABB}
     */
    public static AABB rotateHorizontal(AABB box, Direction side) {
        return switch (side) {
            case NORTH -> rotate(box, Rotation.NONE);
            case SOUTH -> rotate(box, Rotation.CLOCKWISE_180);
            case WEST -> rotate(box, Rotation.COUNTERCLOCKWISE_90);
            case EAST -> rotate(box, Rotation.CLOCKWISE_90);
            default -> box;
        };
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
        return rotate(shape, side, VoxelShapeUtils::rotate);
    }

    /**
     * Rotates a {@link VoxelShape} according to a specific rotation.
     *
     * @param shape    The {@link VoxelShape} to rotate
     * @param rotation The rotation we are performing.
     *
     * @return The rotated {@link VoxelShape}
     */
    public static VoxelShape rotate(VoxelShape shape, Rotation rotation) {
        return rotate(shape, rotation, VoxelShapeUtils::rotate);
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
        return rotate(shape, side, VoxelShapeUtils::rotateHorizontal);
    }

    /**
     * Rotates a {@link VoxelShape} using a specific transformation function for each {@link AABB} in the {@link VoxelShape}.
     *
     * @param shape          The {@link VoxelShape} to rotate
     * @param rotateFunction The transformation function to apply to each {@link AABB} in the {@link VoxelShape}.
     *
     * @return The rotated {@link VoxelShape}
     */
    public static VoxelShape rotate(VoxelShape shape, UnaryOperator<AABB> rotateFunction) {
        List<VoxelShape> rotatedPieces = new ArrayList<>();
        //Explode the voxel shape into bounding boxes
        List<AABB> sourceBoundingBoxes = shape.toAabbs();
        //Rotate them and convert them each back into a voxel shape
        for (AABB sourceBoundingBox : sourceBoundingBoxes) {
            //Make the bounding box be centered around the middle, and then move it back after rotating
            rotatedPieces.add(Shapes.create(rotateFunction.apply(sourceBoundingBox.move(fromOrigin.x, fromOrigin.y, fromOrigin.z))
                  .move(-fromOrigin.x, -fromOrigin.z, -fromOrigin.z)));
        }
        //return the recombined rotated voxel shape
        return combine(rotatedPieces);
    }

    /**
     * Rotates a {@link VoxelShape} using a specific transformation function for each {@link AABB} in the {@link VoxelShape}.
     *
     * @param shape          The {@link VoxelShape} to rotate
     * @param rotateFunction The transformation function to apply to each {@link AABB} in the {@link VoxelShape}.
     *
     * @return The rotated {@link VoxelShape}
     */
    public static <DATA> VoxelShape rotate(VoxelShape shape, DATA data, BiFunction<AABB, DATA, AABB> rotateFunction) {
        List<VoxelShape> rotatedPieces = new ArrayList<>();
        //Explode the voxel shape into bounding boxes
        List<AABB> sourceBoundingBoxes = shape.toAabbs();
        //Rotate them and convert them each back into a voxel shape
        for (AABB sourceBoundingBox : sourceBoundingBoxes) {
            //Make the bounding box be centered around the middle, and then move it back after rotating
            rotatedPieces.add(Shapes.create(rotateFunction.apply(sourceBoundingBox.move(fromOrigin.x, fromOrigin.y, fromOrigin.z), data)
                  .move(-fromOrigin.x, -fromOrigin.z, -fromOrigin.z)));
        }
        //return the recombined rotated voxel shape
        return combine(rotatedPieces);
    }

    /**
     * Used for mass combining shapes
     *
     * @param shapes The list of {@link VoxelShape}s to include
     *
     * @return A simplified {@link VoxelShape} including everything that is part of the input shapes.
     */
    public static VoxelShape combine(VoxelShape... shapes) {
        return batchCombine(Shapes.empty(), BooleanOp.OR, true, shapes);
    }

    /**
     * Used for mass combining shapes
     *
     * @param shapes The collection of {@link VoxelShape}s to include
     *
     * @return A simplified {@link VoxelShape} including everything that is part of the input shapes.
     */
    public static VoxelShape combine(Collection<VoxelShape> shapes) {
        return batchCombine(Shapes.empty(), BooleanOp.OR, true, shapes);
    }

    /**
     * Used for cutting shapes out of a full cube
     *
     * @param shapes The list of {@link VoxelShape}s to cut out
     *
     * @return A {@link VoxelShape} including everything that is not part of the input shapes.
     */
    public static VoxelShape exclude(VoxelShape... shapes) {
        return batchCombine(Shapes.block(), BooleanOp.ONLY_FIRST, true, shapes);
    }

    /**
     * Used for mass combining shapes using a specific {@link BooleanOp} and a given start shape.
     *
     * @param initial  The {@link VoxelShape} to start with
     * @param function The {@link BooleanOp} to perform
     * @param simplify True if the returned shape should run {@link VoxelShape#optimize()}, False otherwise
     * @param shapes   The collection of {@link VoxelShape}s to include
     *
     * @return A {@link VoxelShape} based on the input parameters.
     *
     * @implNote We do not do any simplification until after combining all the shapes, and then only if the {@code simplify} is True. This is because there is a
     * performance hit in calculating the simplified shape each time if we still have more changers we are making to it.
     */
    public static VoxelShape batchCombine(VoxelShape initial, BooleanOp function, boolean simplify, Collection<VoxelShape> shapes) {
        VoxelShape combinedShape = initial;
        for (VoxelShape shape : shapes) {
            combinedShape = Shapes.joinUnoptimized(combinedShape, shape, function);
        }
        return simplify ? combinedShape.optimize() : combinedShape;
    }

    /**
     * Used for mass combining shapes using a specific {@link BooleanOp} and a given start shape.
     *
     * @param initial  The {@link VoxelShape} to start with
     * @param function The {@link BooleanOp} to perform
     * @param simplify True if the returned shape should run {@link VoxelShape#optimize()}, False otherwise
     * @param shapes   The list of {@link VoxelShape}s to include
     *
     * @return A {@link VoxelShape} based on the input parameters.
     *
     * @implNote We do not do any simplification until after combining all the shapes, and then only if the {@code simplify} is True. This is because there is a
     * performance hit in calculating the simplified shape each time if we still have more changers we are making to it.
     */
    public static VoxelShape batchCombine(VoxelShape initial, BooleanOp function, boolean simplify, VoxelShape... shapes) {
        VoxelShape combinedShape = initial;
        for (VoxelShape shape : shapes) {
            combinedShape = Shapes.joinUnoptimized(combinedShape, shape, function);
        }
        return simplify ? combinedShape.optimize() : combinedShape;
    }

    public static void setShape(VoxelShape shape, VoxelShape[] dest, boolean verticalAxis) {
        setShape(shape, dest, verticalAxis, false);
    }

    public static void setShape(VoxelShape shape, VoxelShape[] dest, boolean verticalAxis, boolean invert) {
        Direction[] dirs = verticalAxis ? EnumUtils.DIRECTIONS : EnumUtils.HORIZONTAL_DIRECTIONS;
        for (Direction side : dirs) {
            dest[verticalAxis ? side.ordinal() : side.ordinal() - 2] = verticalAxis ? rotate(shape, invert ? side.getOpposite() : side) : rotateHorizontal(shape, side);
        }
    }

    public static void setShape(VoxelShape shape, VoxelShape[] dest) {
        setShape(shape, dest, false, false);
    }
}