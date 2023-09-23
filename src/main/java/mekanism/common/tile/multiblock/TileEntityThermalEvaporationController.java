package mekanism.common.tile.multiblock;

import mekanism.common.content.evaporation.EvaporationMultiblockData;
import mekanism.common.registries.MekanismBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityThermalEvaporationController extends TileEntityThermalEvaporationBlock {

    public TileEntityThermalEvaporationController(BlockPos pos, BlockState state) {
        super(MekanismBlocks.THERMAL_EVAPORATION_CONTROLLER, pos, state);
        delaySupplier = NO_DELAY;
    }

    @Override
    protected boolean onUpdateServer(EvaporationMultiblockData multiblock) {
        boolean needsPacket = super.onUpdateServer(multiblock);
        setActive(multiblock.isFormed());
        return needsPacket;
    }

    @Override
    public boolean canBeMaster() {
        return true;
    }
}