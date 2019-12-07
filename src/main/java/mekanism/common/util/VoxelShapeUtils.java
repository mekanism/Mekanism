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
        VoxelShape frameBack1 = getSlope(-1, 0, 0, 1, 19, 1, 7.5, -6, 6.49,
              0, 0, 0.837758, 0.0625, true);
        VoxelShape frameBack2 = getSlope(0, 0, 0, 1, 19, 1, -7.5, -6, 6.49,
              0, 0, -0.837758, 0.0625, true);
        VoxelShape frameBack4 = getSlope(0, 0, 0, 1, 19, 1, -7.5, 7, 6.49,
              0, 0, -0.837758, 0.0625, true);//*/
        VoxelShape frameBack5 = getSlope(-1, 0, 0, 1, 19, 1, 7.5, 7, 6.49,
              0, 0, 0.837758, 0.0625, true);//*/
        //return combine(frameBack1, frameBack2, frameBack4, frameBack5);

        /*VoxelShape frameLeft5 = getSlope(0, 0, 0, 1, 19, 1, -7.485, 7, -7.5,
              0.837758, 0, 0, 0.0625, true);

        VoxelShape frameRight5 = getSlope(0, 0, 0, 1, 19, 1, 6.485, 7, -7.5,
              0.837758, 0, 0, 0.0625, true);*/

        //While it isn't as efficient to do it for a easy transform lets do so just to make it simpler for now in debugging what is going on
        VoxelShape frameBack3 = getSlope(0, 0, 0, 13, 1, 1, -6.5, 6, 6.5,
              0, 0, 0, 0.0625, true);//*/
        //makeCuboidShape(1.5, 17, 0.5, 14.5, 18, 1.5)
        //0.09375, 1.0625, 0.03125, 0.90625‬, 1.125, 0.09375
        //return frameBack5;
        return combine(frameBack1, frameBack2, frameBack3, frameBack4, frameBack5);
    }

    //TODO: When we make this more of a util method, make it so that we are printing the createSlope thing instead of the params to this
    public static VoxelShape getSlope(double offX, double offY, double offZ, int width, int height, int depth, double rotationPointX, double rotationPointY,
          double rotationPointZ, double rotateAngleX, double rotateAngleY, double rotateAngleZ, double scale, boolean mirror) {
        Mekanism.logger.info("STARTING HERE");
        //TODO: Rotating is not taking into account the fact that we don't really want to start in the center of x and z
        //Transform from mekanism model: (8, 24, 8, 8, 24, 8) - (box + (rotationPoint, rotationPoint))
        //The shift has to do with the initial translation
        //TODO: Fully shift after rotating
        double xShift = 0;//16 * 0.5;
        double yShift = 0;//16 * 1.5;
        double zShift = 0;//16 * 0.5;

        Vec3d rot = calculateTransform(rotationPointX, rotationPointY, rotationPointZ, 0, 0, Math.PI);

        double shiftX = 16 * 0.5 + rot.x;
        double shiftY = 16 * 1.5 + rot.y;
        double shiftZ = 16 * 0.5 + rot.z;
        //?, -6.5 -> 8
        //?, 6 -> 24
        //?, 6.5 -> 8
        //0.6875
        //1.4375
        //TODO: ??? 0.5 + rotationPointX * scale
        // -6.5, 6, 6.5 -> -0.40625, 0.375, 0.40625
        // 0.09375, 1.875, 0.90625
        // then multiply back by 16
        // 1.5, 30, 14.5
        //TODO: ??? 0.5 - rotationPointX * scale
        // 0.90625, 1.125, 0.09375
        // then multiply back by 16
        // 14.5, 18, 1.5
        //TODO: That actually works surprisingly well, other than the first Y value

        double minX = offX;
        double minY = offY;
        double minZ = offZ;
        double maxX = minX + width;
        double maxY = minY + height;
        double maxZ = minZ + depth;

        double shiftedMinX = minX;// + rotationPointX;
        double shiftedMinY = minY;// + rotationPointY;
        double shiftedMinZ = minZ;// + rotationPointZ;
        double shiftedMaxX = maxX;// + rotationPointX;
        double shiftedMaxY = maxY;// + rotationPointY;
        double shiftedMaxZ = maxZ;// + rotationPointZ;

        //translate it by the amount we want to shift it and then subtract (our position translated to our rotation point)
        double xMin = xShift - shiftedMinX;
        double yMin = yShift - shiftedMinY;
        double zMin = zShift - shiftedMinZ;
        double xMax = xShift - shiftedMaxX;
        double yMax = yShift - shiftedMaxY;
        double zMax = zShift - shiftedMaxZ;

        /*Mekanism.logger.info("Positions: {}, {}, {}, {}, {}, {}",
              shiftedMinX, shiftedMinY, shiftedMinZ, shiftedMaxX, shiftedMaxY, shiftedMaxZ);
        Mekanism.logger.info("Positions Shifted: {}, {}, {}, {}, {}, {}",
              shiftedMinX + xShift, shiftedMinY + yShift, shiftedMinZ + zShift, shiftedMaxX + xShift, shiftedMaxY + yShift, shiftedMaxZ + zShift);

        Vec3d start1 = calculateTransform(shiftedMinX + xShift, shiftedMinY + yShift, shiftedMinZ + zShift, rotateAngleX, rotateAngleY, rotateAngleZ);
        Vec3d end1 = calculateTransform(shiftedMaxX + xShift, shiftedMaxY + yShift, shiftedMaxZ + zShift, rotateAngleX, rotateAngleY, rotateAngleZ);
        Mekanism.logger.info("Positions: {}, {}, {}, {}, {}, {}",
              start1.x - xShift, start1.y - yShift, start1.z - zShift, end1.x - xShift, end1.y - yShift, end1.z - zShift);

        Vec3d start2 = calculateTransform(minX, minY, minZ, rotateAngleX, rotateAngleY, rotateAngleZ, mirror);
        Vec3d end2 = calculateTransform(maxX, maxY, maxZ, rotateAngleX, rotateAngleY, rotateAngleZ, mirror);
        Mekanism.logger.info("Positions: {}, {}, {}, {}, {}, {}",
              start2.x, start2.y, start2.z, end2.x, end2.y, end2.z);
        Mekanism.logger.info("Positions: {}, {}, {}, {}, {}, {}",
              start2.x + xShift, start2.y + yShift, start2.z + zShift, end2.x + xShift, end2.y + yShift, end2.z + zShift);*/

        //Vec3d rotMin = calculateTransform(xMax1, yMax1, zMax1, 0, 0, Math.PI);
        //GlStateManager.rotatef(180, 0, 0, 1);
        //TODO: Note these are "backwards" in terms of the min and max being passed
        Vec3d start = calculateTransform(xMax, yMax, zMax, rotateAngleX, rotateAngleY, rotateAngleZ, mirror);
        Vec3d end = calculateTransform(xMin, yMin, zMin, rotateAngleX, rotateAngleY, rotateAngleZ, mirror);



        /*Vec3d shiftedMin = calculateTransform(shiftedMinX, shiftedMinY, shiftedMinZ, rotateAngleX, rotateAngleY, rotateAngleZ, mirror);
        Vec3d shiftedMax = calculateTransform(shiftedMaxX, shiftedMaxY, shiftedMaxZ, rotateAngleX, rotateAngleY, rotateAngleZ, mirror);
        Mekanism.logger.info("Positions: {}, {}, {}, {}, {}, {}",
              xShift - shiftedMin.x, yShift - shiftedMin.y, zShift - shiftedMin.z, xShift - shiftedMax.x, yShift - shiftedMax.y, zShift - shiftedMax.z);*/
        //shiftedMax alt: (6.5, 7.0, 7.5) - start


        //Positions: -11.62976561202356, 12.489938022640237, 1.5099999999999998, 1.8208549145412753, -0.9666888745562829, 0.5099999999999998



        //TODO: Old??
        //Vec3d start = calculateTransform(centerWidth, 0, centerDepth, rotationPointXIn, rotationPointYIn, rotationPointZIn, rotateAngleX, rotateAngleY, rotateAngleZ, scale, mirror);
        //Vec3d end = calculateTransform(centerWidth, height, centerDepth, rotationPointXIn, rotationPointYIn, rotationPointZIn, rotateAngleX, rotateAngleY, rotateAngleZ, scale, mirror);
        //Vec3d center = calculateTransform(centerWidth, centerHeight, centerDepth, rotationPointXIn, rotationPointYIn, rotationPointZIn, rotateAngleX, rotateAngleY, rotateAngleZ, scale, mirror);
        //TODO: Create the shape creator automatically from information about width, height, depth? Or at least for the x and y differences
        // or maybe from the change in height etc

        //TODO: Check if we are even doing the mirror calculations correctly, because if not that could be a big part of why everything is screwed up
        //TODO: Calculate 1 unit, and then see about how things get shifted?
        //Shifted Transformation: (0.669130636796047, 4.253759954424503, 0.5)
        //Shifted Transformation: (14.788881800156927, 16.967242053549395, 0.5)
        //TODO: make this automatic? currently shifts end by 1 so that it calculates including the block it is placing properly
        return createSlope(start.x + shiftX, start.y + shiftY, start.z + shiftZ, end.x + shiftX - 1, end.y + shiftY - 1, end.z + shiftZ - 1, (x, y, z) -> Block.makeCuboidShape(x, y, z, x + 1, y + 1, z + 1));
        //return createSlope(start.x, start.y, start.z, end.x - 1, end.y - 1, end.z - 1, (x, y, z) -> Block.makeCuboidShape(x, y, z, x + 1, y + 1, z + 1));
        //return createSlope(start.x, start.y, start.z, end.x, end.y, end.z, (x, y, z) -> Block.makeCuboidShape(x, y, z, x + 1, y + 1, z + 1));
    }

    //TODO: Rename d, e, f
    private static Vec3d calculateTransform(double x, double y, double z, double rotateAngleX, double rotateAngleY, double rotateAngleZ, boolean mirror) {
        //TODO: I think open gl rotations and stuff changes the origin, and axis
        // So we first have to shift our thing based on the rotation point, and then rotate it
        // Or are we doing it backwards and have to rotate and then shift by the stuff
        Vec3d transformed = calculateTransform(x, y, z, rotateAngleX, rotateAngleY, rotateAngleZ);
        Mekanism.logger.info("Transformation: " + transformed);
        //TODO: this seems to maybe be needed?
        //return transformed.mul(-1, 1, 1);
        return transformed;
    }

    //TODO: Rename d, e, f
    private static Vec3d calculateTransform(double D, double E, double F, double x, double y, double z, double rotateAngleX, double rotateAngleY, double rotateAngleZ,
          double scale, boolean mirror) {
        //TODO: I think open gl rotations and stuff changes the origin, and axis
        // So we first have to shift our thing based on the rotation point, and then rotate it
        // Or are we doing it backwards and have to rotate and then shift by the stuff
        double scaledX = x * scale;
        double scaledY = y * scale;
        double scaledZ = z * scale;
        Vec3d shifted = calculateTransform(scaledX + D, scaledY + E, scaledZ + F, rotateAngleX, rotateAngleY, rotateAngleZ);
        Vec3d alt = calculateTransform(D, E, F, rotateAngleX, rotateAngleY, rotateAngleZ);

        //translate by x, y, z becomes:
        //xTranslated = x + X * 1 = 1 + x
        //yTranslated = y + Y * 1 = 1 + y
        //zTranslated = z + Z * 1 = 1 + z
        /*Vec3d rot = calculateTransform(x - D, y - E, z - F, rotateAngleX, rotateAngleY, rotateAngleZ);
        Vec3d rotUnshift = new Vec3d(D + rot.x, E + rot.y, F + rot.z);

        Vec3d rot2 = calculateTransform(scaledX - D, scaledY - E, scaledZ - F, rotateAngleX, rotateAngleY, rotateAngleZ);
        Vec3d rot2Unshift = new Vec3d(D + rot2.x, E + rot2.y, F + rot2.z);

        Vec3d rot3 = calculateTransform(scaledX, scaledY, scaledZ, rotateAngleX, rotateAngleY, rotateAngleZ);
        Vec3d rot3Shift = new Vec3d(D + rot3.x, E + rot3.y, F + rot3.z);
        Mekanism.logger.info("Stuff: {}, {}, {}, {}, {}, {}", rot3Shift.x, rot3Shift.y, rot3Shift.z, scaledX, scaledY, scaledZ);
        Vec3d rot3Unshift = new Vec3d(rot3Shift.x - scaledX, rot3Shift.y - scaledY, rot3Shift.z - scaledZ);*/
        //scaled values:
        //VoxelShape frameBack3 = getSlope(0, 0, 0, 13, 1, 1, -6.5, 6, 6.5, 0, 0, 0, 0.0625, true);
        //makeCuboidShape(1.5, 17, 0.5, 14.5, 18, 1.5)
        //Does that mean it really has these coordinates:
        // 0.09375, 1.0625, 0.03125, 0.90625‬, 1.125, 0.09375
        //-0.40625, 0.375, 0.40625
        //x + scaledX, y + scaledY, z + scaledZ
        //x = x + X * pointX
        //y = y + Y * pointY
        //z = z + Z * pointZ
        //
        //Transform from mekanism model: (8, 24, 8, 8, 24, 8) - (box + (rotationPoint, rotationPoint))
        //8 - (D + rotX)
        /*Vec3d test = new Vec3d(8 - (D + x), 24 - (E + y), 8 - (F + z));
        Vec3d rotTest = calculateTransform(test.x, test.y, test.z, rotateAngleX, rotateAngleY, rotateAngleZ);
        Vec3d rotTest2 = calculateTransform(test.x * scale, test.y * scale, test.z * scale, rotateAngleX, rotateAngleY, rotateAngleZ);
        Mekanism.logger.info("test: " + test + " rotTest: " + rotTest + " rotTest2: " + rotTest2);*/

        //TODO: Mirror doesn't negate x does it, I think mirror might only flip the texture?
        Vec3d transformed = shifted;//mirror ? new Vec3d(-shifted.x, shifted.y, shifted.z) : shifted;
        Mekanism.logger.info("Transformation: " + transformed + " alt: " + alt
                             /*+ " rot: " + rot + " rotUnshift: " + rotUnshift + " rot2: " + rot2 + " rot2Unshift: " + rot2Unshift
                             + " rot3: " + rot3 + " rot3Shift: " + rot3Shift + " rot3Unshift: " + rot3Unshift*/);
        return alt;//transformed;
    }

    /*This is pretty decent in the result it actually gives though the shifting is screwed up
    public static VoxelShape getSlope(double offX, double offY, double offZ, int width, int height, int depth, double rotationPointXIn, double rotationPointYIn,
          double rotationPointZIn, double rotateAngleX, double rotateAngleY, double rotateAngleZ, double scale, boolean mirror) {
        double centerWidth = width / 2.0;
        double centerDepth = depth / 2.0;
        Vec3d start = calculateTransform(centerWidth, 0, centerDepth, rotationPointXIn, rotationPointYIn, rotationPointZIn, rotateAngleX, rotateAngleY, rotateAngleZ, scale, mirror);
        Vec3d end = calculateTransform(centerWidth, height, centerDepth, rotationPointXIn, rotationPointYIn, rotationPointZIn, rotateAngleX, rotateAngleY, rotateAngleZ, scale, mirror);

        //TODO: Is this even partially correct? Because we are not at all touching offY or offZ
        Vec3d shift = calculateTransform(centerWidth - offX, 0, 0, rotationPointXIn, rotationPointYIn, rotationPointZIn, rotateAngleX, rotateAngleY, rotateAngleZ, scale, mirror);
        Vec3d offset = calculateTransform(offX, offY, offZ, rotationPointXIn, rotationPointYIn, rotationPointZIn, rotateAngleX, rotateAngleY, rotateAngleZ, scale, mirror);
        Vec3d rot = calculateTransform(rotationPointXIn, rotationPointYIn, rotationPointZIn, rotationPointXIn, rotationPointYIn, rotationPointZIn, rotateAngleX, rotateAngleY, rotateAngleZ, scale, mirror);
        Vec3d rot2 = calculateTransform(rotationPointXIn, rotationPointYIn, rotationPointZIn, 0, 0, 0, rotateAngleX, rotateAngleY, rotateAngleZ, scale, mirror);
        Vec3d rot3 = calculateTransform(rotationPointXIn, rotationPointYIn, rotationPointZIn, offX, offY, offZ, rotateAngleX, rotateAngleY, rotateAngleZ, scale, mirror);
        //Transformation: (-0.9807542288776925, 2.3969047524961278, 0.81125)
        //TODO: Figure out a way to calculate the shift automatically
        // I think our way of doing the shift partially works in that the 0.81125 works properly for fixing z
        double xShift = shift.x;
        double yShift = -2.6;//shift.y;
        double zShift = shift.z;
        Vec3d shiftedStart = start.subtract(xShift, yShift, zShift);
        Vec3d shiftedEnd = end.subtract(xShift, yShift, zShift);
        Mekanism.logger.info("Shifted Transformation: " + shiftedStart);
        Mekanism.logger.info("Shifted Transformation: " + shiftedEnd);
        //Shifted Transformation: (0.669130636796047, 4.253759954424503, 0.5)
        //Shifted Transformation: (14.788881800156927, 16.967242053549395, 0.5)
        //TODO: Create the shape creator automatically from information about width, height, depth? Or at least for the x and y differences
        // or maybe from the change in height etc
        return createSlope(shiftedStart.x, shiftedStart.y, shiftedStart.z, shiftedEnd.x, shiftedEnd.y, shiftedEnd.z,
              (x, y, z) -> Block.makeCuboidShape(x, y, z, x + 1, y + 1, z + 1));
    }

    private static Vec3d calculateTransform(double D, double E, double F, double x, double y, double z, double rotateAngleX, double rotateAngleY, double rotateAngleZ,
          double scale, boolean mirror) {
        double scaledX = x * scale;
        double scaledY = y * scale;
        double scaledZ = z * scale;
        Vec3d shifted = calculateTransform(scaledX + D, scaledY + E, scaledZ + F, rotateAngleX, rotateAngleY, rotateAngleZ);
        Vec3d origin = calculateTransform(scaledX, scaledY, scaledZ, rotateAngleX, rotateAngleY, rotateAngleZ);
        Vec3d total = shifted.add(origin);
        Vec3d transformed = mirror ? new Vec3d(-total.x, total.y, total.z) : total;
        Mekanism.logger.info("Transformation: " + transformed + " shifted: " + shifted + " origin: " + origin);
        return transformed;
    }
     */

    /*
    public static VoxelShape getSlope(double offX, double offY, double offZ, int width, int height, int depth, double rotationPointXIn, double rotationPointYIn,
          double rotationPointZIn, double rotateAngleX, double rotateAngleY, double rotateAngleZ, double scale, boolean mirror) {
        //double centerWidth = width / 2.0;
        //double centerHeight = height / 2.0;
        //double centerDepth = depth / 2.0;
        double centerWidth = (width + offX) / 2.0;
        double centerHeight = (height + offY) / 2.0;
        double centerDepth = (depth + offZ) / 2.0;

        double scaledRotX = rotationPointXIn * scale;
        double scaledRotY = rotationPointYIn * scale;
        double scaledRotZ = rotationPointZIn * scale;
        Vec3d rot = calculateTransform(rotationPointXIn - centerWidth, rotationPointYIn - centerHeight, rotationPointZIn - centerDepth, rotateAngleX, rotateAngleY, rotateAngleZ);
        Vec3d rotUnshift = new Vec3d(centerWidth + rot.x, centerHeight + rot.y, centerDepth + rot.z);

        Vec3d rot2 = calculateTransform(scaledRotX - centerWidth, scaledRotY - centerHeight, scaledRotZ - centerDepth, rotateAngleX, rotateAngleY, rotateAngleZ);
        Vec3d rot2Unshift = new Vec3d(centerWidth + rot2.x, centerHeight + rot2.y, centerDepth + rot2.z);

        Vec3d rot2Unshift2 = new Vec3d(rot2Unshift.x - scaledRotX, rot2Unshift.y - scaledRotY, rot2Unshift.z - scaledRotZ);
        Mekanism.logger.info("rot: " + rot + " rotUnshift: " + rotUnshift + " rot2: " + rot2 + " rot2Unshift: " + rot2Unshift + " rot2Unshift2: " + rot2Unshift2);
        //7.213839400124227, 3.412780329096086, 0.405625

        Vec3d start = calculateTransform(centerWidth, 0, centerDepth, rotationPointXIn, rotationPointYIn, rotationPointZIn, rotateAngleX, rotateAngleY, rotateAngleZ, scale, mirror);
        Vec3d end = calculateTransform(centerWidth, height, centerDepth, rotationPointXIn, rotationPointYIn, rotationPointZIn, rotateAngleX, rotateAngleY, rotateAngleZ, scale, mirror);
        Vec3d center = calculateTransform(centerWidth, centerHeight, centerDepth, rotationPointXIn, rotationPointYIn, rotationPointZIn, rotateAngleX, rotateAngleY, rotateAngleZ, scale, mirror);
        //Transformation: (6.736781126440605, 7.369407226292605, 0.905625)

        //TODO: Is this even partially correct? Because we are not at all touching offY or offZ
        //Vec3d shift = calculateTransform(centerWidth - offX, 0, 0, rotationPointXIn, rotationPointYIn, rotationPointZIn, rotateAngleX, rotateAngleY, rotateAngleZ, scale, mirror);
        //Vec3d start2 = calculateTransform((width + offX) / 2.0, offY, (depth + offZ) / 2.0, rotationPointXIn, rotationPointYIn, rotationPointZIn, rotateAngleX, rotateAngleY, rotateAngleZ, scale, mirror);
        //Vec3d end2 = calculateTransform((width + offX) / 2.0, height + offY, (depth + offZ) / 2.0, rotationPointXIn, rotationPointYIn, rotationPointZIn, rotateAngleX, rotateAngleY, rotateAngleZ, scale, mirror);
        //Transformation: (-0.9807542288776925, 2.3969047524961278, 0.81125)
        //TODO: Figure out a way to calculate the shift automatically
        // I think our way of doing the shift partially works in that the 0.81125 works properly for fixing z
        double xShift = 0;//shift.x;
        double yShift = 0;//-3.2;//-2.6;//shift.y;
        double zShift = 0;//shift.z;
        Vec3d shiftedStart = start.subtract(xShift, yShift, zShift);
        Vec3d shiftedEnd = end.subtract(xShift, yShift, zShift);
        //Mekanism.logger.info("Shifted Transformation: " + shiftedStart);
        //Mekanism.logger.info("Shifted Transformation: " + shiftedEnd);
        //Shifted Transformation: (0.669130636796047, 4.253759954424503, 0.5)
        //Shifted Transformation: (14.788881800156927, 16.967242053549395, 0.5)
        //TODO: Create the shape creator automatically from information about width, height, depth? Or at least for the x and y differences
        // or maybe from the change in height etc

        //TODO: Check if we are even doing the mirror calculations correctly, because if not that could be a big part of why everything is screwed up
        //TODO: Calculate 1 unit, and then see about how things get shifted?
            return createSlope(shiftedStart.x, shiftedStart.y, shiftedStart.z, shiftedEnd.x, shiftedEnd.y, shiftedEnd.z,
                  (x, y, z) -> Block.makeCuboidShape(x, y, z, x + 1, y + 1, z + 1));
    }

    private static Vec3d calculateTransform(double D, double E, double F, double x, double y, double z, double rotateAngleX, double rotateAngleY, double rotateAngleZ,
          double scale, boolean mirror) {
        //TODO: I think open gl rotations and stuff changes the origin, and axis
        // So we first have to shift our thing based on the rotation point, and then rotate it
        // Or are we doing it backwards and have to rotate and then shift by the stuff
        double scaledX = x * scale;
        double scaledY = y * scale;
        double scaledZ = z * scale;
        Vec3d shifted = calculateTransform(scaledX + D, scaledY + E, scaledZ + F, rotateAngleX, rotateAngleY, rotateAngleZ);
        Vec3d alt = calculateTransform(D, E, F, rotateAngleX, rotateAngleY, rotateAngleZ);
        Vec3d transScaled = calculateTransform(scale + x, scale + y, scale + z, rotateAngleX, rotateAngleY, rotateAngleZ);
        Vec3d transScaled2 = calculateTransform(scale + x + D, scale + y + E, scale + z + F, rotateAngleX, rotateAngleY, rotateAngleZ);
        Vec3d transScaled3 = calculateTransform(D * scale + x, E * scale + y, F * scale + z, rotateAngleX, rotateAngleY, rotateAngleZ);
        Vec3d transScaled4 = calculateTransform(1 + scaledX, 1 + scaledY, 1 + scaledZ, rotateAngleX, rotateAngleY, rotateAngleZ);


        //Rotate it??
        //alt: (-0.3345653183980235, 0.37157239903581263, 0.5)
        //alt: (13.785185844962855, 13.085054498160705, 0.5)
        //And now we translate it?
        Vec3d altTrans = alt.add(scaledX, scaledY, scaledZ);

        //translate by x, y, z becomes:
        //xTranslated = x + X * 1 = 1 + x
        //yTranslated = y + Y * 1 = 1 + y
        //zTranslated = z + Z * 1 = 1 + z
        Vec3d rot = calculateTransform(x - D, y - E, z - F, rotateAngleX, rotateAngleY, rotateAngleZ);
        Vec3d rotUnshift = new Vec3d(D + rot.x, E + rot.y, F + rot.z);

        Vec3d rot2 = calculateTransform(scaledX - D, scaledY - E, scaledZ - F, rotateAngleX, rotateAngleY, rotateAngleZ);
        Vec3d rot2Unshift = new Vec3d(D + rot2.x, E + rot2.y, F + rot2.z);

        Vec3d rot3 = calculateTransform(scaledX, scaledY, scaledZ, rotateAngleX, rotateAngleY, rotateAngleZ);
        Vec3d rot3Shift = new Vec3d(D + rot3.x, E + rot3.y, F + rot3.z);
        Mekanism.logger.info("Stuff: {}, {}, {}, {}, {}, {}", rot3Shift.x, rot3Shift.y, rot3Shift.z, scaledX, scaledY, scaledZ);
        Vec3d rot3Unshift = new Vec3d(rot3Shift.x - scaledX, rot3Shift.y - scaledY, rot3Shift.z - scaledZ);


        //TODO: Mirror doesn't negate x does it, I think mirror might only flip the texture?
        Vec3d transformed = mirror ? new Vec3d(-shifted.x, shifted.y, shifted.z) : shifted;
        Mekanism.logger.info("Transformation: " + transformed + " rot: " + rot + " rotUnshift: " + rotUnshift + " rot2: " + rot2 + " rot2Unshift: " + rot2Unshift
                             + " rot3: " + rot3 + " rot3Shift: " + rot3Shift + " rot3Unshift: " + rot3Unshift);
        return transformed;
    }

     */

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
        int steps = (int) Math.ceil(Math.max(Math.max(Math.abs(xDif), Math.abs(yDif)), Math.abs(zDif)) * 2);
        //int steps = (int) Math.ceil(Math.max(Math.max(Math.abs(xDif), Math.abs(yDif)), Math.abs(zDif)) * 2);
        //Mekanism.logger.info("Differences: " + xDif + ", " + yDif + ", " + zDif + " steps: " + steps);
        //Differences: 14.11975116336088, 12.713482099124892, 0.0
        double tPartial = 1.0 / steps;

        List<VoxelShape> shapes = new ArrayList<>();
        //TODO: Have some max number of steps it is willing to do?
        double x = xStart;
        double y = yStart;
        double z = zStart;
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