package mekanism.common.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.UnaryOperator;
import mekanism.client.model.ModelSeismicVibrator;
import mekanism.common.Mekanism;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.entity.model.RendererModel;
import net.minecraft.client.renderer.model.ModelBox;
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
        //TODO: Add a note for these that we mass combine and THEN simplify due to it being faster,
        // also maybe an option to not simplify so that we can have the partial lines not do all the simplification calculations until we are combining it all
        for (VoxelShape shape : shapes) {
            combinedShape = VoxelShapes.combine(combinedShape, shape, function);
        }
        return combinedShape.simplify();
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
            combinedShape = VoxelShapes.combine(combinedShape, shape, function);
        }
        return combinedShape.simplify();
    }

    public static VoxelShape getSeismicSlope() {
        //TODO: Move this stuff into the SeismicVibrator thing, this is just for making viewing potential inputs easier
        //TODO: Full frame causes lag when looking at it, probably has to do with the corners not quite lining up
        //TODO: Should we round them all to 3 digits of precision before adding them
        ModelSeismicVibrator model = new ModelSeismicVibrator();
        /*return getShapeFromModel(model.frameBack1, model.frameBack2, model.frameBack3, model.frameBack4, model.frameBack5,
              model.frameLeft1, model.frameLeft2, model.frameLeft3, model.frameLeft4, model.frameLeft5,
              model.frameRight1, model.frameRight2, model.frameRight3, model.frameRight4, model.frameRight5);//*/
        return getShapeFromModel(model.frameBack3, model.frameBack5, model.frameLeft5);
    }

    public static VoxelShape getShapeFromModel(RendererModel... models) {
        List<VoxelShape> shapes = new ArrayList<>();
        for (RendererModel model : models) {
            shapes.add(getShapeFromModel(model));
        }
        return combine(shapes);
    }

    //TODO: In JavaDoc note to only call this from the client? Do we also have to do clientSide only so that it doesn't crash the server??
    //TODO: Maybe move it to a client side model util
    public static VoxelShape getShapeFromModel(RendererModel model) {
        List<VoxelShape> shapes = new ArrayList<>();
        for (ModelBox box : model.cubeList) {
            shapes.add(getSlope(box.posX1, box.posY1, box.posZ1, box.posX2, box.posY2, box.posZ2,
                  model.rotationPointX, model.rotationPointY, model.rotationPointZ, model.rotateAngleX, model.rotateAngleY, model.rotateAngleZ));
        }
        if (model.childModels != null) {
            for (RendererModel childModel : model.childModels) {
                shapes.add(getShapeFromModel(childModel));
            }
        }
        return combine(shapes);
    }

    //TODO: When we make this more of a util method, make it so that we are printing the createSlope thing instead of the params to this
    public static VoxelShape getSlope(double minX, double minY, double minZ, double maxX, double maxY, double maxZ, double rotationPointX, double rotationPointY,
          double rotationPointZ, double rotateAngleX, double rotateAngleY, double rotateAngleZ) {
        Mekanism.logger.info("STARTING HERE");
        //Note: This is a manual rotation to not have to deal with numbers getting not rounding properly due to double precision
        double rotX = -rotationPointX;
        double rotY = -rotationPointY;
        double rotZ = rotationPointZ;

        double shiftX = 16 * 0.5 + rotX;
        double shiftY = 16 * 1.5 + rotY;
        double shiftZ = 16 * 0.5 + rotZ;

        Mekanism.logger.info("Corners: {}, {}, {}, {}, {}, {}", minX, minY, minZ, maxX, maxY, maxZ);
        //TODO: Do we need to do center in each one to figure out the proper pieces
        //TODO: Do we want to use these vector's for figuring out either:
        // a. The VoxelShape to create
        // b. The slope in a given direction
        //x angle -> y, z get changed
        //y angle -> x, z get changed
        //z angle -> x, y get changed
        //TODO: I believe this is how we calculate for rotating around x or z
        Vec3d start1 = calculateTransform(minX, minY, minZ, rotateAngleX, rotateAngleY, rotateAngleZ);
        Vec3d start2 = calculateTransform(maxX, minY, minZ, rotateAngleX, rotateAngleY, rotateAngleZ);
        Vec3d start3 = calculateTransform(minX, minY, maxZ, rotateAngleX, rotateAngleY, rotateAngleZ);
        Vec3d start4 = calculateTransform(maxX, minY, maxZ, rotateAngleX, rotateAngleY, rotateAngleZ);
        Vec3d startSum = start1.add(start2).add(start3).add(start4);
        Vec3d startAvg = startSum.mul(0.25, 0.25, 0.25);

        Vec3d end1 = calculateTransform(minX, maxY, minZ, rotateAngleX, rotateAngleY, rotateAngleZ);
        Vec3d end2 = calculateTransform(maxX, maxY, minZ, rotateAngleX, rotateAngleY, rotateAngleZ);
        Vec3d end3 = calculateTransform(minX, maxY, maxZ, rotateAngleX, rotateAngleY, rotateAngleZ);
        Vec3d end4 = calculateTransform(maxX, maxY, maxZ, rotateAngleX, rotateAngleY, rotateAngleZ);
        Vec3d endSum = end1.add(end2).add(end3).add(end4);
        Vec3d endAvg = endSum.mul(0.25, 0.25, 0.25);
        //TODO: Explain the swap of start and end
        //Manually do the 180 rotation around z
        Vec3d end = startAvg.mul(-1, -1, 1);
        Vec3d start = endAvg.mul(-1, -1, 1);

        //TODO: Figure out why back3 doesn't work because until we have straight working also our logic is broken

        //TODO - correct position for back 5, when going from "center":
        //Positions: 14.954316481758903, 4.658090299910921, 14.99, 0.8345653183980235, 17.371572399035813, 14.99
        //TODO: TRY THIS, do the thing I initially tried with subtracting center then rotating then adding center back???

        //TODO: Try shifting x and z results by the amount it took to get to the center
        //TODO: Figure out the proper transforms?? z is incorrect currently it is off by one
        //TODO: I think we need to somehow merge all the different shifts
        Mekanism.logger.info("start: " + start + " end: " + end);

        //TODO: Create the shape creator automatically from information about width, height, depth? Or at least for the x and y differences
        // or maybe from the change in height etc

        //TODO: Check if we are even doing the mirror calculations correctly, because if not that could be a big part of why everything is screwed up
        //TODO: Calculate 1 unit, and then see about how things get shifted?
        double startX = start.x + shiftX;
        double startY = start.y + shiftY;
        double startZ = start.z + shiftZ;
        double endX = end.x + shiftX;
        double endY = end.y + shiftY;
        double endZ = end.z + shiftZ;

        Mekanism.logger.info("Shift: {}, {}, {}", shiftX, shiftY, shiftZ);
        //Shift: 0.5, 17.0, 14.49
        Mekanism.logger.info("Positions: {}, {}, {}, {}, {}, {}", startX, startY, startZ, endX, endY, endZ);
        //Positions: 14.619751163360878, 4.286517900875108, 13.49, 1.1691306367960461, 17.743144798071626, 14.49
        //This is the proper one now - when going from the center:
        //Positions: 14.954316481758903, 4.658090299910921, 14.99, 0.8345653183980235, 17.371572399035813, 14.99

        ShapeCreator shapeCreator = (x, y, z) -> Block.makeCuboidShape(x - 0.5, y - 0.5, z - 0.5, x + 0.5, y + 0.5, z + 0.5);
        double xHalf = (minX + maxX) / 2.0;
        double yHalf = (minY + maxY) / 2.0;
        double zHalf = (minZ + maxZ) / 2.0;
        //ShapeCreator shapeCreator = (x, y, z) -> Block.makeCuboidShape(x - xHalf, y - yHalf, z - zHalf, x + xHalf, y + yHalf, z + zHalf);

        return createSlope(startX, startY, startZ, endX, endY, endZ, shapeCreator);
    }

    private static Vec3d calculateTransform(double x, double y, double z, double rotateAngleX, double rotateAngleY, double rotateAngleZ) {
        double xReturn = x;
        double yReturn = y;
        double zReturn = z;
        if (rotateAngleZ != 0) {
            double sinZ = Math.sin(rotateAngleZ);
            double cosZ = Math.cos(rotateAngleZ);
            xReturn = x * cosZ - y * sinZ;
            yReturn = x * sinZ + y * cosZ;
        }
        if (rotateAngleY != 0) {
            double sinY = Math.sin(rotateAngleY);
            double cosY = Math.cos(rotateAngleY);
            xReturn = x * cosY + z * sinY;
            zReturn = z * cosY - x * sinY;
        }
        if (rotateAngleX != 0) {
            double sinX = Math.sin(rotateAngleX);
            double cosX = Math.cos(rotateAngleX);
            yReturn = y * cosX - z * sinX;
            zReturn = y * sinX + z * cosX;
        }
        return new Vec3d(xReturn, yReturn, zReturn);
    }

    public static VoxelShape createSlope(double xStart, double yStart, double zStart, double xEnd, double yEnd, double zEnd, ShapeCreator shapeCreator) {
        double xDif = xEnd - xStart;
        double yDif = yEnd - yStart;
        double zDif = zEnd - zStart;
        if (xDif == 0 && yDif == 0 && zDif == 0) {
            //If start and end are the same, return an empty voxel shape
            return VoxelShapes.empty();
        }
        //TODO: Figure out the shape creator dynamically and combined with the steps?
        //TODO: Also if 2 of the points don't change we can just draw a straight line
        //TODO: Improve this as it looks like an absolute mess
        int steps = (int) Math.ceil(Math.max(Math.max(Math.abs(xDif), Math.abs(yDif)), Math.abs(zDif)) * 4.0 / 3.0);
        //int steps = (int) Math.ceil(Math.max(Math.max(Math.abs(xDif), Math.abs(yDif)), Math.abs(zDif)) * 2);
        //Mekanism.logger.info("Differences: " + xDif + ", " + yDif + ", " + zDif + " steps: " + steps);
        //Differences: 14.11975116336088, 12.713482099124892, 0.0
        double tPartial = 1.0 / steps;
        Mekanism.logger.info("x = {} + {} * t", xStart, xDif);
        Mekanism.logger.info("y = {} + {} * t", yStart, yDif);
        Mekanism.logger.info("z = {} + {} * t", zStart, zDif);
        //TODO: Make them each have their own values of t, or at least number of steps?
        //x = 14.61975116336088 + -14.450620526564833 * t
        //y = 4.286517900875108 + 12.456626897196518 * t
        //z = 13.49 + 0.0 * t

        //x = 14.61975116336088 + -13.450620526564833 * t
        //y = 4.286517900875108 + 13.456626897196518 * t
        //z = 13.49 + 1.0 * t

        List<VoxelShape> shapes = new ArrayList<>();
        //TODO: Have some max number of steps it is willing to do?
        double x = xStart;
        double y = yStart;
        double z = zStart;
        //TODO: Instead of adding one do we want to start at 1 and then have it be offset towards the inside
        //TODO: Fix when fixing where this assumption is from
        // Note: We add 1 to adjust for the shift for calculating based on shape
        for (int step = 0; step <= steps + 1; step++) {
            shapes.add(shapeCreator.createShape(x, y, z));
            double t = tPartial * step;
            x = xStart + xDif * t;
            y = yStart + yDif * t;
            z = zStart + zDif * t;
        }
        //TODO: Implement a slightly more advanced version of this method that gets used, which allows for custom equations
        return combine(shapes);
    }

    @FunctionalInterface
    public interface ShapeCreator {

        VoxelShape createShape(double x, double y, double z);
    }
}