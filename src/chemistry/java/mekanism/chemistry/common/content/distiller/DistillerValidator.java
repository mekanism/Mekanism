package mekanism.chemistry.common.content.distiller;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import java.util.EnumSet;
import java.util.Set;
import mekanism.chemistry.common.registries.ChemistryBlockTypes;
import mekanism.chemistry.common.tile.multiblock.TileEntityFractionatingDistillerController;
import mekanism.common.MekanismLang;
import mekanism.common.content.blocktype.BlockType;
import mekanism.common.content.evaporation.EvaporationMultiblockData;
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
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

public class DistillerValidator extends CuboidStructureValidator<DistillerMultiblockData> {

    private static final VoxelCuboid MIN_CUBOID = new VoxelCuboid(3, 4, 3);
    private static final VoxelCuboid MAX_CUBOID = new VoxelCuboid(3, 8, 3);

    private boolean foundController = false;

    @Override
    protected FormationResult validateFrame(FormationProtocol<DistillerMultiblockData> ctx, BlockPos pos, BlockState state, CasingType type, boolean needsFrame) {
        boolean controller = structure.getTile(pos) instanceof TileEntityFractionatingDistillerController;
        if (foundController && controller) {
            return FormationResult.fail(MekanismLang.MULTIBLOCK_INVALID_CONTROLLER_CONFLICT, pos);
        }
        foundController |= controller;
        return super.validateFrame(ctx, pos, state, type, needsFrame);
    }

    @Override
    protected CasingType getCasingType(BlockState state) {
        Block block = state.getBlock();
        if (BlockType.is(block, ChemistryBlockTypes.FRACTIONATING_DISTILLER_BLOCK) || BlockType.is(block, ChemistryBlockTypes.FRACTIONATING_DISTILLER_CONTROLLER)) {
            return CasingType.FRAME;
        } else if (BlockType.is(block, ChemistryBlockTypes.FRACTIONATING_DISTILLER_VALVE)) {
            return CasingType.VALVE;
        }
        return CasingType.INVALID;
    }

    @Override
    public boolean precheck() {
        cuboid = StructureHelper.fetchCuboid(structure, MIN_CUBOID, MAX_CUBOID);
        return cuboid != null;
    }

    @Override
    public FormationResult postcheck(DistillerMultiblockData structure, Set<BlockPos> innerNodes, Long2ObjectMap<ChunkAccess> chunkMap) {
        if (!foundController) {
            return FormationResult.fail(MekanismLang.MULTIBLOCK_INVALID_NO_CONTROLLER);
        }
        return FormationResult.SUCCESS;
    }
}
