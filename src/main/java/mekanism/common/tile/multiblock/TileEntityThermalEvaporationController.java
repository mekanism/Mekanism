package mekanism.common.tile.multiblock;

import mekanism.common.content.evaporation.EvaporationMultiblockData;
import mekanism.common.registries.MekanismBlocks;

public class TileEntityThermalEvaporationController extends TileEntityThermalEvaporationBlock {

    public TileEntityThermalEvaporationController() {
        super(MekanismBlocks.THERMAL_EVAPORATION_CONTROLLER);
        delaySupplier = () -> 0;
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