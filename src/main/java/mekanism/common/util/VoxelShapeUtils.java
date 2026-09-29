package mekanism.common.util;

import com.mojang.math.OctahedralGroup;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import mekanism.common.Mekanism;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class VoxelShapeUtils {

    /// Copy of [Shapes#BLOCK_CENTER]
    private static final Vec3 BLOCK_CENTER = new Vec3(0.5, 0.5, 0.5);

    /// Prints out an easy to copy-paste string representing the cuboid of a shape
    public static void print(double x1, double y1, double z1, double x2, double y2, double z2) {
        Mekanism.logger.info("box({}, {}, {}, {}, {}, {}),", Math.min(x1, x2), Math.min(y1, y2), Math.min(z1, z2),
              Math.max(x1, x2), Math.max(y1, y2), Math.max(z1, z2));
    }

    /// Prints out a set of strings that make copy-pasting easier, for simplifying a voxel shape
    public static void printSimplified(String name, VoxelShape shape) {
        Mekanism.logger.info("Simplified: {}", name);
        for (AABB box : shape.optimize().toAabbs()) {
            print(box.minX * 16, box.minY * 16, box.minZ * 16, box.maxX * 16, box.maxY * 16, box.maxZ * 16);
        }
    }

    /// Used for mass combining shapes
    ///
    /// @param shapes The list of [VoxelShape]s to include
    ///
    /// @return A simplified [VoxelShape] including everything that is part of the input shapes.
    public static VoxelShape combine(VoxelShape... shapes) {
        return batchCombine(Shapes.empty(), BooleanOp.OR, true, shapes);
    }

    /// Used for mass combining shapes using a specific [BooleanOp] and a given start shape.
    ///
    /// @param initial  The [VoxelShape] to start with
    /// @param function The [BooleanOp] to perform
    /// @param simplify True if the returned shape should run [VoxelShape#optimize()], False otherwise
    /// @param shapes   The collection of [VoxelShape]s to include
    ///
    /// @return A [VoxelShape] based on the input parameters.
    ///
    /// @implNote We do not do any simplification until after combining all the shapes, and then only if the `simplify` is True. This is because there is a performance
    /// hit in calculating the simplified shape each time if we still have more changers we are making to it.
    public static VoxelShape batchCombine(VoxelShape initial, BooleanOp function, boolean simplify, Collection<VoxelShape> shapes) {
        VoxelShape combinedShape = initial;
        for (VoxelShape shape : shapes) {
            combinedShape = Shapes.joinUnoptimized(combinedShape, shape, function);
        }
        return simplify ? combinedShape.optimize() : combinedShape;
    }

    /// Used for mass combining shapes using a specific [BooleanOp] and a given start shape.
    ///
    /// @param initial  The [VoxelShape] to start with
    /// @param function The [BooleanOp] to perform
    /// @param simplify True if the returned shape should run [VoxelShape#optimize()], False otherwise
    /// @param shapes   The list of [VoxelShape]s to include
    ///
    /// @return A [VoxelShape] based on the input parameters.
    ///
    /// @implNote We do not do any simplification until after combining all the shapes, and then only if the `simplify` is True. This is because there is a performance
    /// hit in calculating the simplified shape each time if we still have more changers we are making to it.
    public static VoxelShape batchCombine(VoxelShape initial, BooleanOp function, boolean simplify, VoxelShape... shapes) {
        VoxelShape combinedShape = initial;
        for (VoxelShape shape : shapes) {
            combinedShape = Shapes.joinUnoptimized(combinedShape, shape, function);
        }
        return simplify ? combinedShape.optimize() : combinedShape;
    }

    @Deprecated//TODO: Try to move the remaining use cases to one of the rotateAll util methods
    public static Map<Direction, VoxelShape> rotateAllLegacy(VoxelShape shape) {
        return new EnumMap<>(Map.of(
              Direction.NORTH, Shapes.rotate(shape, OctahedralGroup.ROT_180_EDGE_YZ_NEG),
              Direction.EAST, Shapes.rotate(shape, OctahedralGroup.ROT_120_PPN),
              Direction.SOUTH, Shapes.rotate(shape, OctahedralGroup.ROT_90_X_POS),
              Direction.WEST, Shapes.rotate(shape, OctahedralGroup.ROT_120_PNP),
              Direction.UP, Shapes.rotate(shape, OctahedralGroup.IDENTITY),
              Direction.DOWN, Shapes.rotate(shape, OctahedralGroup.ROT_180_FACE_YZ)
        ));
    }

    public static Map<Direction, VoxelShape> rotateAllInitialDown(VoxelShape down) {
        return rotateAll(down, OctahedralGroup.BLOCK_ROT_X_270);
    }

    public static Map<Direction, VoxelShape> rotateAll(VoxelShape shape, OctahedralGroup initial) {
        return Shapes.rotateAll(shape, initial, BLOCK_CENTER);
    }

    public static Map<Direction, VoxelShape> rotateHorizontal(VoxelShape shape, OctahedralGroup initial) {
        return Shapes.rotateHorizontal(shape, initial, BLOCK_CENTER);
    }
}