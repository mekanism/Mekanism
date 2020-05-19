package mekanism.generators.common.content.fusion;

import java.util.EnumSet;
import mekanism.common.MekanismLang;
import mekanism.common.lib.math.Cuboid;
import mekanism.common.lib.math.Cuboid.CuboidSide;
import mekanism.common.lib.multiblock.CuboidStructureValidator;
import mekanism.common.lib.multiblock.FormationProtocol;
import mekanism.common.lib.multiblock.FormationProtocol.FormationResult;
import mekanism.common.lib.multiblock.Structure;
import mekanism.common.lib.multiblock.Structure.Axis;
import mekanism.common.lib.multiblock.StructureHelper;
import mekanism.generators.common.tile.fusion.TileEntityFusionReactorController;
import net.minecraft.util.math.BlockPos;

public class FusionReactorStructureValidator extends CuboidStructureValidator {

    private static final Cuboid BOUNDS = new Cuboid(5, 5, 5);
    private static final byte[][] ALLOWED_GRID = new byte[][] {
        {0, 0, 1, 0, 0},
        {0, 1, 2, 1, 0},
        {1, 2, 2, 2, 1},
        {0, 1, 2, 1, 0},
        {0, 0, 1, 0, 0}
    };

    public FusionReactorStructureValidator(Structure structure) {
        super(structure);
    }

    @Override
    public FormationResult validate(FormationProtocol<?> protocol, FormationProtocol<?>.ValidationContext ctx) {
        BlockPos min = cuboid.getMinPos(), max = cuboid.getMaxPos();
        for (int x = min.getX(); x <= max.getX(); x++) {
            for (int y = min.getY(); y <= max.getY(); y++) {
                for (int z = min.getZ(); z <= max.getZ(); z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (cuboid.isOnSide(pos)) {
                        FrameType type = getFrameType(pos);
                        if (type == FrameType.IGNORED) {
                            // validate the frame, but we won't fail if it's invalid
                            ctx.validateFrame(pos, false);
                            continue;
                        } else {
                            boolean isControllerPos = isControllerPos(pos);
                            boolean controller = structure.getTile(pos) instanceof TileEntityFusionReactorController;
                            if (isControllerPos && !controller) {
                                return FormationResult.fail(MekanismLang.MULTIBLOCK_INVALID_NO_CONTROLLER);
                            } else if (!isControllerPos && controller) {
                                return FormationResult.fail(MekanismLang.MULTIBLOCK_INVALID_CONTROLLER_CONFLICT);
                            }
                            FormationResult ret = ctx.validateFrame(pos, type.needsFrame());
                            if (!ret.isFormed()) {
                                return ret;
                            }
                        }
                    } else {
                        FormationResult ret = ctx.validateInner(pos);
                        if (!ret.isFormed()) {
                            return ret;
                        }
                    }
                }
            }
        }
        return FormationResult.SUCCESS;
    }

    private FrameType getFrameType(BlockPos pos) {
        Axis axis = Axis.get(cuboid.getSide(pos));
        Axis h = axis.horizontal(), v = axis.vertical();
        pos = pos.subtract(cuboid.getMinPos());
        return FrameType.FRAMES[ALLOWED_GRID[h.getCoord(pos)][v.getCoord(pos)]];
    }

    private boolean isControllerPos(BlockPos pos) {
        return pos.getY() == cuboid.getMaxPos().getY() && pos.getX() == cuboid.getMinPos().getX() + 2 && pos.getZ() == cuboid.getMinPos().getZ() + 2;
    }

    @Override
    public boolean checkValid() {
        // 72 = (12 missing blocks possible on each face) * (6 sides)
        cuboid = StructureHelper.fetchCuboid(structure, BOUNDS, BOUNDS, EnumSet.allOf(CuboidSide.class), 72);
        return cuboid != null;
    }

    private enum FrameType {
        IGNORED,
        FRAME,
        OTHER;

        private static final FrameType[] FRAMES = values();

        boolean needsFrame() {
            return this == FRAME || this == IGNORED;
        }
    }
}
