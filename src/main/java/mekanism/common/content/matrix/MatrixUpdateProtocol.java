package mekanism.common.content.matrix;

import mekanism.api.Coord4D;
import mekanism.common.Mekanism;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.multiblock.MultiblockCache.CacheSubstance;
import mekanism.common.multiblock.MultiblockManager;
import mekanism.common.multiblock.UpdateProtocol;
import mekanism.common.registries.MekanismBlockTypes;
import mekanism.common.tile.TileEntityInductionCasing;
import mekanism.common.tile.TileEntityInductionCell;
import mekanism.common.tile.TileEntityInductionProvider;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

public class MatrixUpdateProtocol extends UpdateProtocol<MatrixMultiblockData> {

    public MatrixUpdateProtocol(TileEntityInductionCasing tile) {
        super(tile);
    }

    @Override
    protected CasingType getCasingType(BlockPos pos) {
        Block block = pointer.getWorld().getBlockState(pos).getBlock();
        if (BlockTypeTile.is(block, MekanismBlockTypes.INDUCTION_CASING)) {
            return CasingType.FRAME;
        } else if (BlockTypeTile.is(block, MekanismBlockTypes.INDUCTION_PORT)) {
            return CasingType.VALVE;
        }
        return CasingType.INVALID;
    }

    @Override
    public boolean isValidInnerNode(BlockPos pos) {
        if (super.isValidInnerNode(pos)) {
            return true;
        }
        TileEntity tile = MekanismUtils.getTileEntity(pointer.getWorld(), pos);
        return tile instanceof TileEntityInductionCell || tile instanceof TileEntityInductionProvider;
    }

    @Override
    protected MultiblockManager<MatrixMultiblockData> getManager() {
        return Mekanism.matrixManager;
    }

    @Override
    protected void onStructureDestroyed(MatrixMultiblockData structure) {
        //Save all energy changes before destroying the structure
        structure.invalidate();
        super.onStructureDestroyed(structure);
    }

    @Override
    protected boolean shouldCap(CacheSubstance type) {
        return type != CacheSubstance.ENERGY;
    }

    @Override
    protected FormationResult validate(MatrixMultiblockData structure) {
        for (Coord4D coord : innerNodes) {
            TileEntity tile = MekanismUtils.getTileEntity(pointer.getWorld(), coord.getPos());
            if (tile instanceof TileEntityInductionCell) {
                structure.addCell(coord, (TileEntityInductionCell) tile);
            } else if (tile instanceof TileEntityInductionProvider) {
                structure.addProvider(coord, (TileEntityInductionProvider) tile);
            }
        }
        return FormationResult.SUCCESS;
    }
}