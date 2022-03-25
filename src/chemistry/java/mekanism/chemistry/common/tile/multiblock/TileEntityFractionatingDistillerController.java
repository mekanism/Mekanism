package mekanism.chemistry.common.tile.multiblock;

import mekanism.chemistry.common.content.distiller.DistillerMultiblockData;
import mekanism.chemistry.common.registries.ChemistryBlocks;
import mekanism.common.content.evaporation.EvaporationMultiblockData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityFractionatingDistillerController extends TileEntityFractionatingDistillerBlock {
    public TileEntityFractionatingDistillerController(BlockPos pos, BlockState state) {
        super(ChemistryBlocks.FRACTIONATING_DISTILLER_CONTROLLER, pos, state);
        delaySupplier = () -> 0;
    }

    @Override
    protected boolean onUpdateServer(DistillerMultiblockData multiblock) {
        boolean needsPacket = super.onUpdateServer(multiblock);
        setActive(multiblock.isFormed());
        return needsPacket;
    }

    @Override
    public boolean canBeMaster() {
        return true;
    }
}
