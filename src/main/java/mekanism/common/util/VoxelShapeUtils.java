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
        //TODO: Also make a new util method to fully convert a model over
        /*VoxelShape frameBack1 = getSlope(-1, 0, 0, 1, 19, 1, 7.5, -6, 6.49,
              0, 0, 0.837758, 0.0625, true);
        VoxelShape frameBack2 = getSlope(0, 0, 0, 1, 19, 1, -7.5, -6, 6.49,
              0, 0, -0.837758, 0.0625, true);//*/
        //While it isn't as efficient to do it for a easy transform lets do so just to make it simpler for now in debugging what is going on
        VoxelShape frameBack3 = getSlope(0, 0, 0, 13, 1, 1, -6.5, 6, 6.5,
              0, 0, 0, 0.0625, true);
        /*VoxelShape frameBack4 = getSlope(0, 0, 0, 1, 19, 1, -7.5, 7, 6.49,
              0, 0, -0.837758, 0.0625, true);
        VoxelShape frameBack5 = getSlope(-1, 0, 0, 1, 19, 1, 7.5, 7, 6.49,
              0, 0, 0.837758, 0.0625, true);//*/
        //return combine(frameBack1, frameBack2, frameBack4, frameBack5);

        /*VoxelShape frameLeft5 = getSlope(0, 0, 0, 1, 19, 1, -7.485, 7, -7.5,
              0.837758, 0, 0, 0.0625, true);

        VoxelShape frameRight5 = getSlope(0, 0, 0, 1, 19, 1, 6.485, 7, -7.5,
              0.837758, 0, 0, 0.0625, true);//*/


        //makeCuboidShape(1.5, 17, 0.5, 14.5, 18, 1.5)
        //0.09375, 1.0625, 0.03125, 0.90625‬, 1.125, 0.09375
        //return frameBack5;
        //return combine(frameBack1, frameBack2, frameBack3, frameBack4, frameBack5);
        //return combine(frameBack1, frameBack2, frameBack3, frameBack4, frameBack5, frameLeft5, frameRight5);
        return frameBack3;
        //TODO: Full frame causes lag when looking at it, probably has to do with the corners not quite lining up
        //TODO: Should we round them all to 3 digits of precision before adding them
    }

    //TODO: When we make this more of a util method, make it so that we are printing the createSlope thing instead of the params to this
    public static VoxelShape getSlope(double offX, double offY, double offZ, int width, int height, int depth, double rotationPointX, double rotationPointY,
          double rotationPointZ, double rotateAngleX, double rotateAngleY, double rotateAngleZ, double scale, boolean mirror) {
        Mekanism.logger.info("STARTING HERE");
        //Vec3d rot = calculateTransform(rotationPointX, rotationPointY, rotationPointZ, 0, 0, Math.PI, mirror);
        //Note: This is a manual rotation to not have to deal with numbers getting not rounding properly due to double precision
        double rotX = -rotationPointX;//rot.x;
        double rotY = -rotationPointY;//rot.y;
        double rotZ = rotationPointZ;//rot.z;

        double shiftX = 16 * 0.5 + rotX;
        double shiftY = 16 * 1.5 + rotY;
        double shiftZ = 16 * 0.5 + rotZ;

        double minX = offX;
        double minY = offY;
        double minZ = offZ;
        double maxX = minX + width;
        double maxY = minY + height;
        double maxZ = minZ + depth;

        //translate it by the amount we want to shift it and then subtract (our position translated to our rotation point)
        double xMin = -minX;
        double yMin = -minY;
        double zMin = -minZ;
        double xMax = -maxX;
        double yMax = -maxY;
        double zMax = -maxZ;

        //TODO: Note these are "backwards" in terms of the min and max being passed
        //Vec3d start = calculateTransform(xMax, yMax, zMax, rotateAngleX, rotateAngleY, rotateAngleZ, mirror);
        //Vec3d end = calculateTransform(xMin, yMin, zMin, rotateAngleX, rotateAngleY, rotateAngleZ, mirror);

        Mekanism.logger.info("Corners: {}, {}, {}, {}, {}, {}", xMin, yMin, zMin, xMax, yMax, zMax);
        double xCenter = (xMin + xMax) / 2.0;
        double yCenter = (yMin + yMax) / 2.0;
        double zCenter = (zMin + zMax) / 2.0;
        //double xCenter = width / 2.0;
        //double yCenter = height / 2.0;
        //double zCenter = depth / 2.0;
        //TODO: Using the center fixes the angle, won't work for if we are rotating around y though
        //TODO: Do we need to do center in each one to figure out the proper pieces
        //TODO: Do we want to use these vector's for figuring out either:
        // a. The VoxelShape to create
        // b. The slope in a given direction
        //x angle -> y, z get changed
        //y angle -> x, z get changed
        //z angle -> x, y get changed
        //Vec3d xStartVec = calculateTransform(xMax, yCenter, zCenter, rotateAngleX, rotateAngleY, rotateAngleZ, mirror);
        //Vec3d xEndVec = calculateTransform(xMin, yCenter, zCenter, rotateAngleX, rotateAngleY, rotateAngleZ, mirror);
        //Vec3d yStartVec = calculateTransform(xCenter, yMax, zCenter, rotateAngleX, rotateAngleY, rotateAngleZ, mirror);
        //Vec3d yEndVec = calculateTransform(xCenter, yMin, zCenter, rotateAngleX, rotateAngleY, rotateAngleZ, mirror);
        //Vec3d zStartVec = calculateTransform(xCenter, yCenter, zMax, rotateAngleX, rotateAngleY, rotateAngleZ, mirror);
        //Vec3d zEndVec = calculateTransform(xCenter, yCenter, zMin, rotateAngleX, rotateAngleY, rotateAngleZ, mirror);
        //Vec3d center = calculateTransform(xCenter, yCenter, zCenter, rotateAngleX, rotateAngleY, rotateAngleZ, mirror);
        //Vec3d max = calculateTransform(xMax, yMax, zMax, rotateAngleX, rotateAngleY, rotateAngleZ, mirror);
        //Vec3d min = calculateTransform(xMin, yMin, zMin, rotateAngleX, rotateAngleY, rotateAngleZ, mirror);
        //Vec3d start = yStartVec;//new Vec3d(max.x, yStartVec.y, max.z);
        //Vec3d end = yEndVec;//new Vec3d(min.x, yEndVec.y, min.z);

        //Vec3d one = calculateTransform(1, 1, 1, rotateAngleX, rotateAngleY, rotateAngleZ, mirror);
        //(-0.07401416127557825, 1.4122754348676723, 1.0)
        //Vec3d oneX = calculateTransform(1, 0, 0, rotateAngleX, rotateAngleY, rotateAngleZ, mirror);
        //(0.669130636796047, 0.7431447980716253, 0.0)
        //Vec3d oneY = calculateTransform(0, 1, 0, rotateAngleX, rotateAngleY, rotateAngleZ, mirror);
        //(-0.7431447980716253, 0.669130636796047, 0.0)
        //Vec3d oneZ = calculateTransform(0, 0, 1, rotateAngleX, rotateAngleY, rotateAngleZ, mirror);
        //(0.0, 0.0, 1.0)
        //For width, height, depth
        //endCorner: (-13.450620526564833, 13.456626897196518, 1.0)

        /*
        Vec3d start1 = calculateTransform(minX, minY, minZ, rotateAngleX, rotateAngleY, rotateAngleZ, mirror);
        Vec3d start2 = calculateTransform(maxX, minY, minZ, rotateAngleX, rotateAngleY, rotateAngleZ, mirror);
        Vec3d start3 = calculateTransform(minX, minY, maxZ, rotateAngleX, rotateAngleY, rotateAngleZ, mirror);
        Vec3d start4 = calculateTransform(maxX, minY, maxZ, rotateAngleX, rotateAngleY, rotateAngleZ, mirror);
        Vec3d startSum = start1.add(start2).add(start3).add(start4);
        Vec3d startAvg = startSum.mul(0.25, 0.25, 0.25);

        Vec3d end1 = calculateTransform(minX, maxY, minZ, rotateAngleX, rotateAngleY, rotateAngleZ, mirror);
        Vec3d end2 = calculateTransform(maxX, maxY, minZ, rotateAngleX, rotateAngleY, rotateAngleZ, mirror);
        Vec3d end3 = calculateTransform(minX, maxY, maxZ, rotateAngleX, rotateAngleY, rotateAngleZ, mirror);
        Vec3d end4 = calculateTransform(maxX, maxY, maxZ, rotateAngleX, rotateAngleY, rotateAngleZ, mirror);
        Vec3d endSum = end1.add(end2).add(end3).add(end4);
        Vec3d endAvg = endSum.mul(0.25, 0.25, 0.25);

        end = startAvg.mul(-1, -1, 1);
        start = endAvg.mul(-1, -1, 1);
         */
        Vec3d start1 = calculateTransform(minX, minY, minZ, rotateAngleX, rotateAngleY, rotateAngleZ, mirror);
        Vec3d start2 = calculateTransform(maxX, minY, minZ, rotateAngleX, rotateAngleY, rotateAngleZ, mirror);
        Vec3d start3 = calculateTransform(minX, minY, maxZ, rotateAngleX, rotateAngleY, rotateAngleZ, mirror);
        Vec3d start4 = calculateTransform(maxX, minY, maxZ, rotateAngleX, rotateAngleY, rotateAngleZ, mirror);
        Vec3d startSum = start1.add(start2).add(start3).add(start4);
        Vec3d startAvg = startSum.mul(0.25, 0.25, 0.25);

        Vec3d end1 = calculateTransform(minX, maxY, minZ, rotateAngleX, rotateAngleY, rotateAngleZ, mirror);
        Vec3d end2 = calculateTransform(maxX, maxY, minZ, rotateAngleX, rotateAngleY, rotateAngleZ, mirror);
        Vec3d end3 = calculateTransform(minX, maxY, maxZ, rotateAngleX, rotateAngleY, rotateAngleZ, mirror);
        Vec3d end4 = calculateTransform(maxX, maxY, maxZ, rotateAngleX, rotateAngleY, rotateAngleZ, mirror);
        Vec3d endSum = end1.add(end2).add(end3).add(end4);
        Vec3d endAvg = endSum.mul(0.25, 0.25, 0.25);

        //TODO: Figure out why back3 doesn't work because until we have straight working also our logic is broken
        //Manually do the 180 rotation around z
        Vec3d end = startAvg.mul(-1, -1, 1);
        Vec3d start = endAvg.mul(-1, -1, 1);
        //[14:03:03] [Client thread/INFO] [Mekanism/]: Transformation: (0.0, 0.0, 0.0)
        //[14:03:03] [Client thread/INFO] [Mekanism/]: Transformation: (13.0, 0.0, 0.0)
        //[14:03:03] [Client thread/INFO] [Mekanism/]: Transformation: (0.0, 0.0, 1.0)
        //[14:03:03] [Client thread/INFO] [Mekanism/]: Transformation: (13.0, 0.0, 1.0)
        //[14:03:03] [Client thread/INFO] [Mekanism/]: Transformation: (0.0, 1.0, 0.0)
        //[14:03:03] [Client thread/INFO] [Mekanism/]: Transformation: (13.0, 1.0, 0.0)
        //[14:03:03] [Client thread/INFO] [Mekanism/]: Transformation: (0.0, 1.0, 1.0)
        //[14:03:03] [Client thread/INFO] [Mekanism/]: Transformation: (13.0, 1.0, 1.0)

        //TODO - correct position for back 5, when going from "center":
        //Positions: 14.954316481758903, 4.658090299910921, 14.99, 0.8345653183980235, 17.371572399035813, 14.99
        //TODO: TRY THIS, do the thing I initially tried with subtracting center then rotating then adding center back???

        //Corners: 1.0, -0.0, -0.0, -0.0, -19.0, -1.0
        //xStartVec: (7.05987558168044, -6.356741049562446, -0.5)
        //xEndVec: (7.729006218476487, -5.61359625149082, -0.5)
        //yStartVec: (14.454316481758903, -12.34190970008908, -0.5)
        //yEndVec: (0.3345653183980235, 0.37157239903581263, -0.5)
        //zStartVec: (7.394440900078463, -5.985168650526633, -1.0)
        //zEndVec: (7.394440900078463, -5.985168650526633, -0.0)
        //TODO: I think this point is important
        //center: (7.394440900078463, -5.985168650526633, -0.5)
        //max: (14.11975116336088, -12.713482099124892, -1.0)
        //min: (0.669130636796047, 0.7431447980716253, -0.0)
        //start: (14.454316481758903, -12.34190970008908, -0.5) end: (0.3345653183980235, 0.37157239903581263, -0.5) xCenter: 0.5 zCenter: -0.5
        //Positions: 14.954316481758903, 4.658090299910921, 13.99, 0.8345653183980235, 17.371572399035813, 13.99

        //using half size instead of half position:
        //[12:06:24] [Client thread/INFO] [Mekanism/]: Transformation: (-7.05987558168044, 6.356741049562446, 0.5)
        //[12:06:24] [Client thread/INFO] [Mekanism/]: Transformation: (-6.390744944884393, 7.099885847634072, 0.5)
        //[12:06:24] [Client thread/INFO] [Mekanism/]: Transformation: (14.454316481758903, -12.34190970008908, 0.5)
        //[12:06:24] [Client thread/INFO] [Mekanism/]: Transformation: (0.3345653183980235, 0.37157239903581263, 0.5)
        //[12:06:24] [Client thread/INFO] [Mekanism/]: Transformation: (-6.725310263282417, 6.728313448598259, -1.0)
        //[12:06:24] [Client thread/INFO] [Mekanism/]: Transformation: (-6.725310263282417, 6.728313448598259, -0.0)
        //[12:06:24] [Client thread/INFO] [Mekanism/]: Transformation: (-6.725310263282417, 6.728313448598259, 0.5)
        //[12:06:24] [Client thread/INFO] [Mekanism/]: start: (14.454316481758903, -12.34190970008908, 0.5) end: (0.3345653183980235, 0.37157239903581263, 0.5) xCenter: 0.5 zCenter: 0.5
        //[12:06:24] [Client thread/INFO] [Mekanism/]: Positions: 14.954316481758903, 4.658090299910921, 14.99, 0.8345653183980235, 17.371572399035813, 14.99

        //TODO: Try shifting x and z results by the amount it took to get to the center
        //TODO: Figure out the proper transforms?? z is incorrect currently it is off by one
        //TODO: I think we need to somehow merge all the different shifts
        Mekanism.logger.info("start: " + start + " end: " + end + " xCenter: " + xCenter + " zCenter: " + zCenter);
        //TODO: This is how it is done to get the correct angle
        //Vec3d start = calculateTransform(xCenter, yMax, zCenter, rotateAngleX, rotateAngleY, rotateAngleZ, mirror);
        //Vec3d end = calculateTransform(xCenter, yMin, zCenter, rotateAngleX, rotateAngleY, rotateAngleZ, mirror);
        //Transformation: (14.11975116336088, -12.713482099124892, -1.0)
        //Transformation: (0.669130636796047, 0.7431447980716253, -0.0)

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

        //TODO: FIXME I am 90% sure that the issue is that it is starting the cube that it draws from the wrong corner at times
        // This can be seen by changing z to be z + 2?
        //return createSlope(startX, startY, endZ, endX - 1, endY - 1, startZ + 1, (x, y, z) -> Block.makeCuboidShape(x, y, z, x + 1, y + 1, z + 1));
        //return createSlope(startX, startY, startZ, endX, endY, endZ, (x, y, z) -> Block.makeCuboidShape(x, y, z, x + 1, y + 1, z + 1));
        //return createSlope(startX, startY, endZ, endX, endY, endZ, (x, y, z) -> Block.makeCuboidShape(x, y, z, x + 1, y + 1, z + 1));
        return createSlope(startX, startY, startZ, endX, endY, endZ, (x, y, z) -> Block.makeCuboidShape(x - 0.5, y - 0.5, z - 0.5, x + 0.5, y + 0.5, z + 0.5));

        //return createSlope(startX, startY, startZ, endX - 1, endY - 1, endZ - 1, (x, y, z) -> Block.makeCuboidShape(x, y, z, x + 1, y + 1, z + 1));
    }

    private static Vec3d calculateTransform(double x, double y, double z, double rotateAngleX, double rotateAngleY, double rotateAngleZ, boolean mirror) {
        Vec3d transformed = calculateTransform(x, y, z, rotateAngleX, rotateAngleY, rotateAngleZ);
        Mekanism.logger.info("Transformation: " + transformed);
        return transformed;
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