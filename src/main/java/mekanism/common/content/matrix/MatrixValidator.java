package mekanism.common.content.matrix;

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
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

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
    public boolean validateInner(BlockPos pos) {
        if (super.validateInner(pos)) {
            return true;
        }
        TileEntity tile = MekanismUtils.getTileEntity(world, pos);
        if (tile instanceof TileEntityInductionCell) {
            cells.add((TileEntityInductionCell) tile);
        } else if (tile instanceof TileEntityInductionProvider) {
            providers.add((TileEntityInductionProvider) tile);
        }
        return tile instanceof TileEntityInductionCell || tile instanceof TileEntityInductionProvider;
    }

    @Override
    public FormationResult postcheck(MatrixMultiblockData structure, Set<BlockPos> innerNodes) {
        cells.forEach(structure::addCell);
        providers.forEach(structure::addProvider);
        return FormationResult.SUCCESS;
    }
}