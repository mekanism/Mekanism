package mekanism.common.content.matrix;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import java.util.ArrayList;
import java.util.List;
import mekanism.common.content.blocktype.BlockType;
import mekanism.common.lib.multiblock.CuboidStructureValidator;
import mekanism.common.lib.multiblock.FormationProtocol.CasingType;
import mekanism.common.lib.multiblock.FormationProtocol.FormationResult;
import mekanism.common.registries.MekanismBlockTypes;
import mekanism.common.tile.multiblock.TileEntityInductionCell;
import mekanism.common.tile.multiblock.TileEntityInductionProvider;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

public class MatrixValidator extends CuboidStructureValidator<MatrixMultiblockData> {

    private final List<TileEntityInductionCell> cells = new ArrayList<>();
    private final List<TileEntityInductionProvider> providers = new ArrayList<>();

    @Override
    protected CasingType getCasingType(BlockState state) {
        Block block = state.getBlock();
        if (BlockType.is(block, MekanismBlockTypes.INDUCTION_CASING)) {
            return CasingType.FRAME;
        } else if (BlockType.is(block, MekanismBlockTypes.INDUCTION_PORT)) {
            return CasingType.VALVE;
        }
        return CasingType.INVALID;
    }

    @Override
    public boolean validateInner(BlockState state, Long2ObjectMap<ChunkAccess> chunkMap, BlockPos pos) {
        if (super.validateInner(state, chunkMap, pos)) {
            return true;
        }
        if (BlockType.is(state.getBlock(), MekanismBlockTypes.BASIC_INDUCTION_CELL, MekanismBlockTypes.ADVANCED_INDUCTION_CELL,
              MekanismBlockTypes.ELITE_INDUCTION_CELL, MekanismBlockTypes.ULTIMATE_INDUCTION_CELL, MekanismBlockTypes.BASIC_INDUCTION_PROVIDER,
              MekanismBlockTypes.ADVANCED_INDUCTION_PROVIDER, MekanismBlockTypes.ELITE_INDUCTION_PROVIDER, MekanismBlockTypes.ULTIMATE_INDUCTION_PROVIDER)) {
            //Compare blocks against the type before bothering to look up the tile
            BlockEntity tile = WorldUtils.getTileEntity(world, chunkMap, pos);
            if (tile instanceof TileEntityInductionCell cell) {
                cells.add(cell);
                return true;
            } else if (tile instanceof TileEntityInductionProvider provider) {
                providers.add(provider);
                return true;
            }
            //Else something went wrong
        }
        return false;
    }

    @Override
    public FormationResult postcheck(MatrixMultiblockData structure, Long2ObjectMap<ChunkAccess> chunkMap) {
        for (TileEntityInductionCell cell : cells) {
            structure.addCell(cell);
        }
        for (TileEntityInductionProvider provider : providers) {
            structure.addProvider(provider);
        }
        return FormationResult.SUCCESS;
    }
}