package mekanism.common.content.evaporation;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
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
import mekanism.common.lib.multiblock.StructureHelper;
import mekanism.common.registries.MekanismBlockTypes;
import mekanism.common.tile.multiblock.TileEntityThermalEvaporationController;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

public class EvaporationValidator extends CuboidStructureValidator<EvaporationMultiblockData> {

    private static final VoxelCuboid MIN_CUBOID = new VoxelCuboid(4, 3, 4);
    private static final VoxelCuboid MAX_CUBOID = new VoxelCuboid(4, 18, 4);

    private boolean foundController = false;

    @Override
    protected FormationResult validateFrame(FormationProtocol<EvaporationMultiblockData> ctx, BlockPos pos, BlockState state, CasingType type, boolean needsFrame) {
        boolean controller = structure.getTile(pos) instanceof TileEntityThermalEvaporationController;
        if (foundController && controller) {
            //Ensure we don't allow ignoring the failure as if there are multiple in the corners which are ignored spots
            // it is possible then we will form with multiple controllers
            return FormationResult.fail(MekanismLang.MULTIBLOCK_INVALID_CONTROLLER_CONFLICT, pos, true);
        }
        foundController |= controller;
        return super.validateFrame(ctx, pos, state, type, needsFrame);
    }

    @Override
    protected StructureRequirement getStructureRequirement(BlockPos pos) {
        WallRelative relative = cuboid.getWallRelative(pos);
        if (pos.getY() == cuboid.getMaxPos().getY()) {
            if (relative.isOnCorner()) {
                return StructureRequirement.IGNORED;
            } else if (!relative.isOnEdge()) {
                return StructureRequirement.INNER;
            } else {
                return StructureRequirement.OTHER;
            }
        }
        return super.getStructureRequirement(pos);
    }

    @Override
    protected CasingType getCasingType(BlockState state) {
        Block block = state.getBlock();
        if (BlockType.is(block, MekanismBlockTypes.THERMAL_EVAPORATION_BLOCK)) {
            return CasingType.FRAME;
        } else if (BlockType.is(block, MekanismBlockTypes.THERMAL_EVAPORATION_VALVE)) {
            return CasingType.VALVE;
        } else if (BlockType.is(block, MekanismBlockTypes.THERMAL_EVAPORATION_CONTROLLER)) {
            return CasingType.OTHER;
        }
        return CasingType.INVALID;
    }

    @Override
    public boolean precheck() {
        cuboid = StructureHelper.fetchCuboid(structure, MIN_CUBOID, MAX_CUBOID, EnumSet.complementOf(EnumSet.of(CuboidSide.TOP)), 8);
        return cuboid != null;
    }

    @Override
    public FormationResult postcheck(EvaporationMultiblockData structure, Long2ObjectMap<ChunkAccess> chunkMap) {
        if (!foundController) {
            return FormationResult.fail(MekanismLang.MULTIBLOCK_INVALID_NO_CONTROLLER);
        }
        return FormationResult.SUCCESS;
    }
}
