package mekanism.common.content.matrix;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import mekanism.common.content.blocktype.BlockType;
import mekanism.common.lib.multiblock.CuboidStructureValidator;
import mekanism.common.lib.multiblock.FormationProtocol.CasingType;
import mekanism.common.lib.multiblock.FormationProtocol.FormationResult;
import mekanism.common.registries.MekanismBlockTypes;
import mekanism.common.tile.multiblock.TileEntityInductionCell;
import mekanism.common.tile.multiblock.TileEntityInductionProvider;
import mekanism.common.util.WorldUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.IChunk;

public class MatrixValidator extends CuboidStructureValidator<MatrixMultiblockData> {

    private final List<TileEntityInductionCell> cells = new ArrayList<>();
    private final List<TileEntityInductionProvider> providers = new ArrayList<>();

    @Override
    protected CasingType getCasingType(BlockPos pos, BlockState state) {
        Block block = state.getBlock();
        if (BlockType.is(block, MekanismBlockTypes.INDUCTION_CASING)) {
            return CasingType.FRAME;
        } else if (BlockType.is(block, MekanismBlockTypes.INDUCTION_PORT)) {
            return CasingType.VALVE;
        }
        return CasingType.INVALID;
    }

    @Override
    public boolean validateInner(BlockState state, Long2ObjectMap<IChunk> chunkMap, BlockPos pos) {
        if (super.validateInner(state, chunkMap, pos)) {
            return true;
        }
        if (BlockType.is(state.getBlock(), MekanismBlockTypes.BASIC_INDUCTION_CELL, MekanismBlockTypes.ADVANCED_INDUCTION_CELL,
              MekanismBlockTypes.ELITE_INDUCTION_CELL, MekanismBlockTypes.ULTIMATE_INDUCTION_CELL, MekanismBlockTypes.BASIC_INDUCTION_PROVIDER,
              MekanismBlockTypes.ADVANCED_INDUCTION_PROVIDER, MekanismBlockTypes.ELITE_INDUCTION_PROVIDER, MekanismBlockTypes.ULTIMATE_INDUCTION_PROVIDER)) {
            //Compare blocks against the type before bothering to lookup the tile
            TileEntity tile = WorldUtils.getTileEntity(world, chunkMap, pos);
            if (tile instanceof TileEntityInductionCell) {
                cells.add((TileEntityInductionCell) tile);
                return true;
            } else if (tile instanceof TileEntityInductionProvider) {
                providers.add((TileEntityInductionProvider) tile);
                return true;
            }
            //Else something went wrong
        }
        return false;
    }

    @Override
    public FormationResult postcheck(MatrixMultiblockData structure, Set<BlockPos> innerNodes, Long2ObjectMap<IChunk> chunkMap) {
        cells.forEach(structure::addCell);
        providers.forEach(structure::addProvider);
        return FormationResult.SUCCESS;
    }
}