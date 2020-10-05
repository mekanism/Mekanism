package mekanism.common.content.sps;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.EnumSet;
import java.util.Set;
import mekanism.common.MekanismLang;
import mekanism.common.content.blocktype.BlockType;
import mekanism.common.lib.math.voxel.VoxelCuboid;
import mekanism.common.lib.math.voxel.VoxelCuboid.CuboidSide;
import mekanism.common.lib.math.voxel.VoxelCuboid.WallRelative;
import mekanism.common.lib.multiblock.CuboidStructureValidator;
import mekanism.common.lib.multiblock.FormationProtocol.CasingType;
import mekanism.common.lib.multiblock.FormationProtocol.FormationResult;
import mekanism.common.lib.multiblock.FormationProtocol.StructureRequirement;
import mekanism.common.lib.multiblock.IValveHandler.ValveData;
import mekanism.common.lib.multiblock.Structure.Axis;
import mekanism.common.lib.multiblock.StructureHelper;
import mekanism.common.registries.MekanismBlockTypes;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public class SPSValidator extends CuboidStructureValidator<SPSMultiblockData> {

    private static final VoxelCuboid BOUNDS = new VoxelCuboid(7, 7, 7);
    private static final byte[][] ALLOWED_GRID = new byte[][]{
          {0, 0, 1, 1, 1, 0, 0},
          {0, 1, 2, 2, 2, 1, 0},
          {1, 2, 2, 2, 2, 2, 1},
          {1, 2, 2, 2, 2, 2, 1},
          {1, 2, 2, 2, 2, 2, 1},
          {0, 1, 2, 2, 2, 1, 0},
          {0, 0, 1, 1, 1, 0, 0}
    };

    @Override
    protected StructureRequirement getStructureRequirement(BlockPos pos) {
        WallRelative relative = cuboid.getWallRelative(pos);
        if (relative.isWall()) {
            Axis axis = Axis.get(cuboid.getSide(pos));
            Axis h = axis.horizontal(), v = axis.vertical();
            pos = pos.subtract(cuboid.getMinPos());
            return StructureRequirement.REQUIREMENTS[ALLOWED_GRID[h.getCoord(pos)][v.getCoord(pos)]];
        }
        return super.getStructureRequirement(pos);
    }

    @Override
    protected CasingType getCasingType(BlockPos pos, BlockState state) {
        Block block = state.getBlock();
        if (BlockType.is(block, MekanismBlockTypes.SPS_CASING)) {
            return CasingType.FRAME;
        } else if (BlockType.is(block, MekanismBlockTypes.SPS_PORT)) {
            return CasingType.VALVE;
        }
        return CasingType.INVALID;
    }

    @Override
    protected boolean validateInner(BlockPos pos) {
        if (super.validateInner(pos)) {
            return true;
        }
        return BlockType.is(world.getBlockState(pos).getBlock(), MekanismBlockTypes.SUPERCHARGED_COIL);
    }

    @Override
    public boolean precheck() {
        // 72 = (12 missing blocks possible on each face) * (6 sides)
        cuboid = StructureHelper.fetchCuboid(structure, BOUNDS, BOUNDS, EnumSet.allOf(CuboidSide.class), 72);
        return cuboid != null;
    }

    @Override
    public FormationResult postcheck(SPSMultiblockData structure, Set<BlockPos> innerNodes) {
        Set<BlockPos> validCoils = new ObjectOpenHashSet<>();
        for (ValveData valve : structure.valves) {
            BlockPos pos = valve.location.offset(valve.side.getOpposite());
            if (innerNodes.contains(pos)) {
                structure.addCoil(valve.location, valve.side.getOpposite());
                validCoils.add(pos);
            }
        }
        // fail if there's a coil not connected to a port
        if (innerNodes.stream().anyMatch(coil -> !validCoils.contains(coil))) {
            return FormationResult.fail(MekanismLang.SPS_INVALID_DISCONNECTED_COIL);
        }
        return FormationResult.SUCCESS;
    }
}
