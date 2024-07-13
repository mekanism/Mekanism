package mekanism.generators.common.content.fusion;

import java.util.EnumSet;
import mekanism.common.MekanismLang;
import mekanism.common.content.blocktype.BlockType;
import mekanism.common.lib.math.voxel.VoxelCuboid;
import mekanism.common.lib.math.voxel.VoxelCuboid.CuboidSide;
import mekanism.common.lib.math.voxel.VoxelCuboid.WallRelative;
import mekanism.common.lib.multiblock.CuboidStructureValidator;
import mekanism.common.lib.multiblock.FormationProtocol;
import mekanism.common.lib.multiblock.FormationProtocol.CasingType;
import mekanism.common.lib.multiblock.FormationProtocol.FormationResult;
import mekanism.common.lib.multiblock.FormationProtocol.StructureRequirement;
import mekanism.common.lib.multiblock.Structure.Axis;
import mekanism.common.lib.multiblock.StructureHelper;
import mekanism.generators.common.registries.GeneratorsBlockTypes;
import mekanism.generators.common.tile.fusion.TileEntityFusionReactorController;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class FusionReactorValidator extends CuboidStructureValidator<FusionReactorMultiblockData> {

    private static final VoxelCuboid BOUNDS = new VoxelCuboid(5, 5, 5);
    private static final byte[][] ALLOWED_GRID = {
          {0, 0, 1, 0, 0},
          {0, 1, 2, 1, 0},
          {1, 2, 2, 2, 1},
          {0, 1, 2, 1, 0},
          {0, 0, 1, 0, 0}
    };

    @Override
    protected StructureRequirement getStructureRequirement(BlockPos pos) {
        WallRelative relative = cuboid.getWallRelative(pos);
        if (relative.isWall()) {
            Axis axis = Axis.get(cuboid.getSide(pos));
            Axis h = axis.horizontal(), v = axis.vertical();
            //Note: This ends up becoming immutable by doing this but that is fine and doesn't really matter
            pos = pos.subtract(cuboid.getMinPos());
            return StructureRequirement.REQUIREMENTS[ALLOWED_GRID[h.getCoord(pos)][v.getCoord(pos)]];
        }
        return super.getStructureRequirement(pos);
    }

    @Override
    protected FormationResult validateFrame(FormationProtocol<FusionReactorMultiblockData> ctx, BlockPos pos, BlockState state, CasingType type, boolean needsFrame) {
        boolean isControllerPos = pos.getY() == cuboid.getMaxPos().getY() && pos.getX() == cuboid.getMinPos().getX() + 2 && pos.getZ() == cuboid.getMinPos().getZ() + 2;
        boolean controller = structure.getTile(pos) instanceof TileEntityFusionReactorController;
        if (isControllerPos && !controller) {
            return FormationResult.fail(MekanismLang.MULTIBLOCK_INVALID_NO_CONTROLLER);
        } else if (!isControllerPos && controller) {
            //When the controller is potentially outside the multiblock we need to make sure to not allow ignoring the failure
            // as otherwise we may allow duplicate controllers
            return FormationResult.fail(MekanismLang.MULTIBLOCK_INVALID_CONTROLLER_CONFLICT, pos, true);
        }
        return super.validateFrame(ctx, pos, state, type, needsFrame);
    }

    @Override
    protected CasingType getCasingType(BlockState state) {
        Block block = state.getBlock();
        if (BlockType.is(block, GeneratorsBlockTypes.FUSION_REACTOR_FRAME)) {
            return CasingType.FRAME;
        } else if (BlockType.is(block, GeneratorsBlockTypes.FUSION_REACTOR_PORT)) {
            return CasingType.VALVE;
        } else if (BlockType.is(block, GeneratorsBlockTypes.FUSION_REACTOR_CONTROLLER,
              GeneratorsBlockTypes.FUSION_REACTOR_LOGIC_ADAPTER, GeneratorsBlockTypes.LASER_FOCUS_MATRIX)) {
            return CasingType.OTHER;
        }
        return CasingType.INVALID;
    }

    @Override
    public boolean precheck() {
        // 72 = (12 missing blocks possible on each face) * (6 sides)
        cuboid = StructureHelper.fetchCuboid(structure, BOUNDS, BOUNDS, EnumSet.allOf(CuboidSide.class), 72);
        return cuboid != null;
    }
}
