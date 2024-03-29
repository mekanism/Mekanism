package mekanism.common.content.sps;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
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
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

public class SPSValidator extends CuboidStructureValidator<SPSMultiblockData> {

    private static final VoxelCuboid BOUNDS = new VoxelCuboid(7, 7, 7);
    private static final byte[][] ALLOWED_GRID = {
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
            //Note: This ends up becoming immutable by doing this but that is fine and doesn't really matter
            pos = pos.subtract(cuboid.getMinPos());
            return StructureRequirement.REQUIREMENTS[ALLOWED_GRID[h.getCoord(pos)][v.getCoord(pos)]];
        }
        return super.getStructureRequirement(pos);
    }

    @Override
    protected CasingType getCasingType(BlockState state) {
        Block block = state.getBlock();
        if (BlockType.is(block, MekanismBlockTypes.SPS_CASING)) {
            return CasingType.FRAME;
        } else if (BlockType.is(block, MekanismBlockTypes.SPS_PORT)) {
            return CasingType.VALVE;
        }
        return CasingType.INVALID;
    }

    @Override
    protected boolean validateInner(BlockState state, Long2ObjectMap<ChunkAccess> chunkMap, BlockPos pos) {
        if (super.validateInner(state, chunkMap, pos)) {
            return true;
        }
        return BlockType.is(state.getBlock(), MekanismBlockTypes.SUPERCHARGED_COIL);
    }

    @Override
    public boolean precheck() {
        // 72 = (12 missing blocks possible on each face) * (6 sides)
        cuboid = StructureHelper.fetchCuboid(structure, BOUNDS, BOUNDS, EnumSet.allOf(CuboidSide.class), 72);
        return cuboid != null;
    }

    @Override
    public FormationResult postcheck(SPSMultiblockData structure, Long2ObjectMap<ChunkAccess> chunkMap) {
        Set<BlockPos> validCoils = new ObjectOpenHashSet<>();
        BlockPos.MutableBlockPos valvePos = new BlockPos.MutableBlockPos();
        for (ValveData valve : structure.valves) {
            valvePos.setWithOffset(valve.location, valve.side.getOpposite());
            if (structure.internalLocations.contains(valvePos)) {
                structure.addCoil(valve.location, valve.side.getOpposite());
                validCoils.add(valvePos.immutable());
            }
        }
        // fail if there's a coil not connected to a port
        // Note: As we only support coils as internal multiblocks for the SPS we can just compare the size of the sets
        if (structure.internalLocations.size() != validCoils.size()) {
            return FormationResult.fail(MekanismLang.SPS_INVALID_DISCONNECTED_COIL);
        }
        return FormationResult.SUCCESS;
    }
}
