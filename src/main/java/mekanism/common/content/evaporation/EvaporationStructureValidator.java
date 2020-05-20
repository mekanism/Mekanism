package mekanism.common.content.evaporation;

import java.util.EnumSet;
import mekanism.common.MekanismLang;
import mekanism.common.lib.math.Cuboid;
import mekanism.common.lib.math.Cuboid.CuboidSide;
import mekanism.common.lib.multiblock.CuboidStructureValidator;
import mekanism.common.lib.multiblock.FormationProtocol;
import mekanism.common.lib.multiblock.FormationProtocol.FormationResult;
import mekanism.common.tile.multiblock.TileEntityThermalEvaporationController;
import mekanism.common.lib.multiblock.Structure;
import mekanism.common.lib.multiblock.StructureHelper;
import net.minecraft.util.math.BlockPos;

public class EvaporationStructureValidator extends CuboidStructureValidator {

    private static final Cuboid MIN_CUBOID = new Cuboid(4, 3, 4);
    private static final Cuboid MAX_CUBOID = new Cuboid(4, 18, 4);

    public EvaporationStructureValidator(Structure structure) {
        super(structure);
    }

    @Override
    public FormationResult validate(FormationProtocol<?> protocol, FormationProtocol<?>.ValidationContext ctx) {
        boolean foundController = false;
        BlockPos min = cuboid.getMinPos(), max = cuboid.getMaxPos();
        for (int x = min.getX(); x <= max.getX(); x++) {
            for (int y = min.getY(); y <= max.getY(); y++) {
                for (int z = min.getZ(); z <= max.getZ(); z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (cuboid.isOnSide(pos) && !isIgnoredSpot(pos)) {
                        if (isSolarSpot(pos)) {
                            // validate the frame, but we won't fail if it's invalid
                            ctx.validateFrame(pos, false);
                            continue;
                        } else {
                            boolean controller = structure.getTile(pos) instanceof TileEntityThermalEvaporationController;
                            if (foundController && controller) {
                                return FormationResult.fail(MekanismLang.MULTIBLOCK_INVALID_CONTROLLER_CONFLICT, pos);
                            }
                            foundController |= controller;
                            FormationResult ret = ctx.validateFrame(pos, cuboid.isOnEdge(pos));
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
        if (!foundController) {
            return FormationResult.fail(MekanismLang.MULTIBLOCK_INVALID_NO_CONTROLLER);
        }
        return FormationResult.SUCCESS;
    }

    private boolean isIgnoredSpot(BlockPos pos) {
        if (pos.getY() == cuboid.getMaxPos().getY() && pos.getX() > cuboid.getMinPos().getX() && pos.getX() < cuboid.getMaxPos().getX() &&
              pos.getZ() > cuboid.getMinPos().getZ() && pos.getZ() < cuboid.getMaxPos().getZ()) {
            return true;
        }
        return false;
    }

    private boolean isSolarSpot(BlockPos pos) {
        return pos.getY() == cuboid.getMaxPos().getY() && cuboid.isOnCorner(pos);
    }

    @Override
    public boolean precheck() {
        cuboid = StructureHelper.fetchCuboid(structure, MIN_CUBOID, MAX_CUBOID, EnumSet.complementOf(EnumSet.of(CuboidSide.TOP)), 8);
        return cuboid != null;
    }
}
