package mekanism.additions.common.content.blocktype;

import java.util.Map;
import mekanism.common.util.VoxelShapeUtils;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class BlockShapes {

    private BlockShapes() {
    }

    private static VoxelShape box(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        return Block.box(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public static final Map<Direction, VoxelShape> GLOW_PANEL = VoxelShapeUtils.rotateAllInitialDown(VoxelShapeUtils.combine(
          box(4, 14, 4, 12, 16, 12),
          box(5, 13.5, 5, 11, 14, 11)
    ));
}
