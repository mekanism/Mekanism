package mekanism.common.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.UnaryOperator;
import mekanism.common.Mekanism;
import net.minecraft.block.Block;
import net.minecraft.util.Direction;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;

public final class VoxelShapeUtils {

    private static final Vector3d fromOrigin = new Vector3d(-0.5, -0.5, -0.5);

    public static void print(double x1, double y1, double z1, double x2, double y2, double z2) {
        Mekanism.logger.info("makeCuboidShape({}, {}, {}, {}, {}, {}),", Math.min(x1, x2), Math.min(y1, y2), Math.min(z1, z2),
              Math.max(x1, x2), Math.max(y1, y2), Math.max(z1, z2));
    }

    /**
     * Prints out a set of strings that make copy pasting easier, for simplifying a voxel shape
     */
    public static void printSimplified(String name, VoxelShape shape) {
        Mekanism.logger.info("Simplified: {}", name);
        shape.simplify().toBoundingBoxList().forEach(box -> print(box.minX * 16, box.minY * 16, box.minZ * 16, box.maxX * 16, box.maxY * 16, box.maxZ * 16));
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
                return new AxisAlignedBB(-box.minX, box.minZ, -box.minY, -box.maxX, box.maxZ, -box.maxY);
            case WEST:
                return new AxisAlignedBB(box.minY, -box.minZ, -box.minX, box.maxY, -box.maxZ, -box.maxX);
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
     * @return A simplified {@link VoxelShape} including everything that is part of any of the input shapes.
     */
    public static VoxelShape combine(VoxelShape... shapes) {
        return batchCombine(VoxelShapes.empty(), IBooleanFunction.OR, true, shapes);
    }

    /**
     * Used for mass combining shapes
     *
     * @param shapes The collection of {@link VoxelShape}s to include
     *
     * @return A simplified {@link VoxelShape} including everything that is part of any of the input shapes.
     */
    public static VoxelShape combine(Collection<VoxelShape> shapes) {
        return combine(shapes, true);
    }

    public static VoxelShape combine(Collection<VoxelShape> shapes, boolean simplify) {
        return batchCombine(VoxelShapes.empty(), IBooleanFunction.OR, simplify, shapes);
    }

    /**
     * Used for cutting shapes out of a full cube
     *
     * @param shapes The list of {@link VoxelShape}s to cut out
     *
     * @return A {@link VoxelShape} including everything that is not part of any of the input shapes.
     */
    public static VoxelShape exclude(VoxelShape... shapes) {
        return batchCombine(VoxelShapes.fullCube(), IBooleanFunction.ONLY_FIRST, true, shapes);
    }

    /**
     * Used for mass combining shapes using a specific {@link IBooleanFunction} and a given start shape.
     *
     * @param initial  The {@link VoxelShape} to start with
     * @param function The {@link IBooleanFunction} to perform
     * @param simplify True if the returned shape should run {@link VoxelShape#simplify()}, False otherwise
     * @param shapes   The collection of {@link VoxelShape}s to include
     *
     * @return A {@link VoxelShape} based on the input parameters.
     *
     * @implNote We do not do any simplification until after combining all the shapes, and then only if the {@code simplify} is True. This is because there is a
     * performance hit in calculating the simplified shape each time if we still have more changers we are making to it.
     */
    public static VoxelShape batchCombine(VoxelShape initial, IBooleanFunction function, boolean simplify, Collection<VoxelShape> shapes) {
        VoxelShape combinedShape = initial;
        for (VoxelShape shape : shapes) {
            combinedShape = VoxelShapes.combine(combinedShape, shape, function);
        }
        return simplify ? combinedShape.simplify() : combinedShape;
    }

    /**
     * Used for mass combining shapes using a specific {@link IBooleanFunction} and a given start shape.
     *
     * @param initial  The {@link VoxelShape} to start with
     * @param function The {@link IBooleanFunction} to perform
     * @param simplify True if the returned shape should run {@link VoxelShape#simplify()}, False otherwise
     * @param shapes   The list of {@link VoxelShape}s to include
     *
     * @return A {@link VoxelShape} based on the input parameters.
     *
     * @implNote We do not do any simplification until after combining all the shapes, and then only if the {@code simplify} is True. This is because there is a
     * performance hit in calculating the simplified shape each time if we still have more changers we are making to it.
     */
    public static VoxelShape batchCombine(VoxelShape initial, IBooleanFunction function, boolean simplify, VoxelShape... shapes) {
        VoxelShape combinedShape = initial;
        for (VoxelShape shape : shapes) {
            combinedShape = VoxelShapes.combine(combinedShape, shape, function);
        }
        return simplify ? combinedShape.simplify() : combinedShape;
    }

    //TODO: Document, figures out the slope, currently has hardcoded shift and rotation based on our most common one we perform to models ahead of time
    public static VoxelShape getSlope(float minX, float minY, float minZ, float maxX, float maxY, float maxZ, float rotationPointX, float rotationPointY,
          float rotationPointZ, float rotateAngleX, float rotateAngleY, float rotateAngleZ, boolean print) {
        float shiftX = 16 * 0.5F - rotationPointX;
        float shiftY = 16 * 1.5F - rotationPointY;
        float shiftZ = 16 * 0.5F + rotationPointZ;

        if (rotateAngleX == 0 && rotateAngleY == 0 && rotateAngleZ == 0) {
            //TODO: This is a shortcut but we may want to make below stuff properly be able to handle angles of zero?
            // My guess is they just don't handle rotating around the y axis at all
            if (print) {
                print(-maxX + shiftX, -maxY + shiftY, maxZ + shiftZ, -minX + shiftX, -minY + shiftY, minZ + shiftZ);
                return VoxelShapes.empty();
            }
            return Block.makeCuboidShape(-maxX + shiftX, -maxY + shiftY, maxZ + shiftZ, -minX + shiftX, -minY + shiftY, minZ + shiftZ);
        } else if (print) {
            Vec3f min = rotateVector(minX, minY, minZ, rotateAngleX, rotateAngleY, rotateAngleZ).mul(-1, -1, 1);
            Vec3f max = rotateVector(maxX, minY, maxZ, rotateAngleX, rotateAngleY, rotateAngleZ).mul(-1, -1, 1);
            float xCenter = (minX + maxX) / 2F;
            float zCenter = (minZ + maxZ) / 2F;
            Vec3f minCenter = rotateVector(xCenter, minY, zCenter, rotateAngleX, rotateAngleY, rotateAngleZ).mul(-1, -1, 1);
            Vec3f maxCenter = rotateVector(xCenter, minY, zCenter, rotateAngleX, rotateAngleY, rotateAngleZ).mul(-1, -1, 1);
            Mekanism.logger.info("Min: {}, Max: {}, MinCenter: {}, MaxCenter: {}", min.add(shiftX, shiftY, shiftZ), max.add(shiftX, shiftY, shiftZ),
                  minCenter.add(shiftX, shiftY, shiftZ), maxCenter.add(shiftX, shiftY, shiftZ));
            return VoxelShapes.empty();
        }
        Vec3f start1 = rotateVector(minX, minY, minZ, rotateAngleX, rotateAngleY, rotateAngleZ);
        Vec3f start2 = rotateVector(maxX, minY, minZ, rotateAngleX, rotateAngleY, rotateAngleZ);
        Vec3f start3 = rotateVector(minX, minY, maxZ, rotateAngleX, rotateAngleY, rotateAngleZ);
        Vec3f start4 = rotateVector(maxX, minY, maxZ, rotateAngleX, rotateAngleY, rotateAngleZ);
        Vec3f startSum = start1.add(start2).add(start3).add(start4);
        Vec3f startAvg = startSum.scale(0.25F);

        Vec3f end1 = rotateVector(minX, maxY, minZ, rotateAngleX, rotateAngleY, rotateAngleZ);
        Vec3f end2 = rotateVector(maxX, maxY, minZ, rotateAngleX, rotateAngleY, rotateAngleZ);
        Vec3f end3 = rotateVector(minX, maxY, maxZ, rotateAngleX, rotateAngleY, rotateAngleZ);
        Vec3f end4 = rotateVector(maxX, maxY, maxZ, rotateAngleX, rotateAngleY, rotateAngleZ);
        Vec3f endSum = end1.add(end2).add(end3).add(end4);
        Vec3f endAvg = endSum.scale(0.25F);
        //TODO: Explain the swap of start and end
        //Manually do the 180 rotation around z
        Vec3f end = startAvg.mul(-1, -1, 1);
        Vec3f start = endAvg.mul(-1, -1, 1);
        //TODO: This is a decent way of calculating the true center point
        // We need to come up with a good way of doing things that have dimensions of things more than 1x1x1
        // And then create a custom shape for them
        //Mekanism.logger.info("start: {} end: {}", start, end);
        //TODO: Create the shape creator automatically from information about width, height, depth? Or at least for the x and y differences
        // or maybe from the change in height etc
        float startX = start.x + shiftX;
        float startY = start.y + shiftY;
        float startZ = start.z + shiftZ;
        float endX = end.x + shiftX;
        float endY = end.y + shiftY;
        float endZ = end.z + shiftZ;

        //Mekanism.logger.info("Shift: {}, {}, {}", shiftX, shiftY, shiftZ);
        //Mekanism.logger.info("Positions: {}, {}, {}, {}, {}, {}", startX, startY, startZ, endX, endY, endZ);

        ShapeCreator shapeCreator = (x, y, z) -> Block.makeCuboidShape(x - 0.5F, y - 0.5F, z - 0.5F, x + 0.5F, y + 0.5F, z + 0.5F);
        //float xHalf = -(minX + maxX) / 2F;
        //float yHalf = -(minY + maxY) / 2F;
        //float zHalf = (minZ + maxZ) / 2F;
        //Mekanism.logger.info("Half: {}, {}, {}, {}, {}, {}", xHalf, yHalf, zHalf, calculateTransform(xHalf, yHalf, zHalf, rotateAngleX, rotateAngleY, rotateAngleZ), start1, end4);
        //TODO: Lag when we have overly complex boxes comes from GameRenderer#updateCameraAndRender - outline and probably: WorldRenderer#drawSelectionBox
        return createSlope(startX, startY, startZ, endX, endY, endZ, shapeCreator);
    }

    /**
     * Rotates a point around the origin with a given angle in the X, Y, and Z directions.
     *
     * @param x            X coordinate of the point to rotate
     * @param y            Y coordinate of the point to rotate
     * @param z            Z coordinate of the point to rotate
     * @param rotateAngleX Angle in radians in the X direction to rotate the input point
     * @param rotateAngleY Angle in radians in the Y direction to rotate the input point
     * @param rotateAngleZ Angle in radians in the Z direction to rotate the input point
     *
     * @return A {@link Vec3f} of the new positions for x, y, and z after rotating around the origin at the given angle.
     */
    private static Vec3f rotateVector(float x, float y, float z, float rotateAngleX, float rotateAngleY, float rotateAngleZ) {
        //TODO: It does not seem that multi angle rotations work properly, at least when one of them is y
        float xReturn = x;
        float yReturn = y;
        float zReturn = z;
        if (rotateAngleZ != 0) {
            float sinZ = (float) Math.sin(rotateAngleZ);
            float cosZ = (float) Math.cos(rotateAngleZ);
            xReturn = x * cosZ - y * sinZ;
            yReturn = x * sinZ + y * cosZ;
            x = xReturn;
            y = yReturn;
        }
        if (rotateAngleY != 0) {
            float sinY = (float) Math.sin(rotateAngleY);
            float cosY = (float) Math.cos(rotateAngleY);
            xReturn = x * cosY + z * sinY;
            zReturn = z * cosY - x * sinY;
            x = xReturn;
            z = zReturn;
        }
        if (rotateAngleX != 0) {
            float sinX = (float) Math.sin(rotateAngleX);
            float cosX = (float) Math.cos(rotateAngleX);
            yReturn = y * cosX - z * sinX;
            zReturn = y * sinX + z * cosX;
        }
        return new Vec3f(xReturn, yReturn, zReturn);
    }

    //TODO: Document, creates a slope between two points building shapes based on shape creator at intervals we create new shapes at
    public static VoxelShape createSlope(float xStart, float yStart, float zStart, float xEnd, float yEnd, float zEnd, ShapeCreator shapeCreator) {
        float xDif = xEnd - xStart;
        float yDif = yEnd - yStart;
        float zDif = zEnd - zStart;
        if (xDif == 0 && yDif == 0 && zDif == 0) {
            //If start and end are the same, return an empty voxel shape
            return VoxelShapes.empty();
        }
        //TODO: Figure out the shape creator dynamically and combined with the steps?
        //TODO: Also if 2 of the points don't change we can just draw a straight line
        //TODO: Improve this as it looks like an absolute mess
        /*if (xDif == 0 && (yDif == 0 || zDif == 0) || yDif == 0 && zDif == 0) {
            //TODO: two are zero draw straight line
        } else if (xDif == 0 || yDif == 0 || zDif == 0) {
            //TODO: only one is zero take the smaller of the two
        }*/

        int steps = (int) Math.ceil(Math.max(Math.max(Math.abs(xDif), Math.abs(yDif)), Math.abs(zDif)) * 4.0 / 3.0);
        //int steps = (int) Math.ceil(Math.max(Math.max(Math.abs(xDif), Math.abs(yDif)), Math.abs(zDif)) * 2);
        //Mekanism.logger.info("Differences: {}, {}, {} steps: {}", xDif, yDif, zDif, steps);
        double tPartial = 1.0 / steps;
        //Mekanism.logger.info("x = {} + {} * t", xStart, xDif);
        //Mekanism.logger.info("y = {} + {} * t", yStart, yDif);
        //Mekanism.logger.info("z = {} + {} * t", zStart, zDif);
        //TODO: Make them each have their own values of t, or at least number of steps?

        List<VoxelShape> shapes = new ArrayList<>();
        //TODO: Have some max number of steps it is willing to do?
        //TODO: Decide which to use
        for (int step = 0; step <= steps; step++) {
            //TODO: I think the lag has to do with how accurate it gets with the x y and z calculations
            // especially with calculating t dynamically
            float t = (float) (tPartial * step);
            float x = xStart + xDif * t;
            float y = yStart + yDif * t;
            float z = zStart + zDif * t;
            shapes.add(shapeCreator.createShape(x, y, z));
        }
        return combine(shapes, false);
    }

    public static VoxelShape translate(VoxelShape shape, Vector3d translation) {
        List<VoxelShape> rotatedPieces = new ArrayList<>();
        List<AxisAlignedBB> sourceBoundingBoxes = shape.toBoundingBoxList();
        for (AxisAlignedBB sourceBoundingBox : sourceBoundingBoxes) {
            rotatedPieces.add(VoxelShapes.create(sourceBoundingBox.offset(translation)));
        }
        return combine(rotatedPieces);
    }

    public static void setShape(VoxelShape shape, VoxelShape[] dest, boolean verticalAxis) {
        setShape(shape, dest, verticalAxis, false);
    }

    public static void setShape(VoxelShape shape, VoxelShape[] dest, boolean verticalAxis, boolean invert) {
        Direction[] dirs = verticalAxis ? EnumUtils.DIRECTIONS : EnumUtils.HORIZONTAL_DIRECTIONS;
        for (Direction side : dirs) {
            dest[verticalAxis ? side.ordinal() : side.ordinal() - 2] = verticalAxis ? VoxelShapeUtils.rotate(shape, invert ? side.getOpposite() : side) : VoxelShapeUtils.rotateHorizontal(shape, side);
        }
    }

    public static void setShape(VoxelShape shape, VoxelShape[] dest) {
        setShape(shape, dest, false, false);
    }

    @FunctionalInterface
    public interface ShapeCreator {

        VoxelShape createShape(float x, float y, float z);
    }

    /**
     * Float version of Vector3d
     */
    private static class Vec3f {

        public final float x;
        public final float y;
        public final float z;

        public Vec3f(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public Vec3f scale(float factor) {
            return mul(factor, factor, factor);
        }

        public Vec3f subtract(Vec3f vec) {
            return subtract(vec.x, vec.y, vec.z);
        }

        public Vec3f subtract(float x, float y, float z) {
            return add(-x, -y, -z);
        }

        public Vec3f add(Vec3f vec) {
            return add(vec.x, vec.y, vec.z);
        }

        public Vec3f add(float x, float y, float z) {
            return new Vec3f(this.x + x, this.y + y, this.z + z);
        }

        public Vec3f mul(float factorX, float factorY, float factorZ) {
            return new Vec3f(this.x * factorX, this.y * factorY, this.z * factorZ);
        }

        @Override
        public String toString() {
            return "(" + this.x + ", " + this.y + ", " + this.z + ")";
        }
    }
}