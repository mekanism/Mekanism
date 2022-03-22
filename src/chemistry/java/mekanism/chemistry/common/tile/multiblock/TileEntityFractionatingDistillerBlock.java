package mekanism.chemistry.common.tile.multiblock;

import mekanism.api.providers.IBlockProvider;
import mekanism.chemistry.common.MekanismChemistry;
import mekanism.chemistry.common.content.distiller.DistillerMultiblockData;
import mekanism.chemistry.common.registries.ChemistryBlocks;
import mekanism.common.lib.multiblock.MultiblockManager;
import mekanism.common.tile.prefab.TileEntityMultiblock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityFractionatingDistillerBlock extends TileEntityMultiblock<DistillerMultiblockData> {
    public TileEntityFractionatingDistillerBlock(BlockPos pos, BlockState state) {
        this(ChemistryBlocks.FRACTIONATING_DISTILLER_BLOCK, pos, state);
    }

    public TileEntityFractionatingDistillerBlock(IBlockProvider blockProvider, BlockPos pos, BlockState state) {
        super(blockProvider, pos, state);
    }

    @Override
    public DistillerMultiblockData createMultiblock() {
        return new DistillerMultiblockData(this);
    }

    @Override
    public MultiblockManager<DistillerMultiblockData> getManager() {
        return MekanismChemistry.distillerManager;
    }

    @Override
    public boolean canBeMaster() {
        return false;
    }
}
