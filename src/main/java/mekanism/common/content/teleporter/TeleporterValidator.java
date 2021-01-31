package mekanism.common.content.teleporter;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import mekanism.common.content.blocktype.BlockType;
import mekanism.common.lib.math.voxel.VoxelCuboid;
import mekanism.common.lib.multiblock.CuboidStructureValidator;
import mekanism.common.lib.multiblock.FormationProtocol;
import mekanism.common.lib.multiblock.FormationProtocol.CasingType;
import mekanism.common.lib.multiblock.StructureHelper;
import mekanism.common.registries.MekanismBlockTypes;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.IChunk;

public class TeleporterValidator extends CuboidStructureValidator<TeleporterMultiblockData> {

    public static final VoxelCuboid MIN_BOUNDS = new VoxelCuboid(1, 1, 1);
    public static final VoxelCuboid MAX_BOUNDS = new VoxelCuboid(17, 18, 17);

    public TeleporterValidator() {
        super(MIN_BOUNDS, MAX_BOUNDS);
    }

    @Override
    protected CasingType getCasingType(BlockPos pos, BlockState state) {
        Block block = state.getBlock();
        if (BlockType.is(block, MekanismBlockTypes.TELEPORTER)) {
            return CasingType.FRAME;
        }
        if (BlockType.is(block, MekanismBlockTypes.TELEPORTER_FRAME)) {
            return CasingType.FRAME;
        }
        return CasingType.INVALID;
    }

    @Override
    protected boolean validateInner(BlockState state, Long2ObjectMap<IChunk> chunkMap, BlockPos pos) {
        return getCasingType(pos, state) != CasingType.FRAME;
    }

    @Override
    protected FormationProtocol.StructureRequirement getStructureRequirement(BlockPos pos) {
            VoxelCuboid.WallRelative relative = cuboid.getWallRelative(pos);
        if (relative == VoxelCuboid.WallRelative.SIDE) {
            return FormationProtocol.StructureRequirement.INNER;
        }
        return super.getStructureRequirement(pos);
    }

    @Override
    public boolean precheck() {
        cuboid = StructureHelper.fetchCuboid(structure, MIN_BOUNDS, MAX_BOUNDS, false);
        return cuboid != null;
    }
}
