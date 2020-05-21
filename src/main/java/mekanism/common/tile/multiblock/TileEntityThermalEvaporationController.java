package mekanism.common.tile.multiblock;

import mekanism.common.registries.MekanismBlocks;

public class TileEntityThermalEvaporationController extends TileEntityThermalEvaporationBlock {

    public TileEntityThermalEvaporationController() {
        super(MekanismBlocks.THERMAL_EVAPORATION_CONTROLLER);
    }

    @Override
    public void onUpdateServer() {
        super.onUpdateServer();
        setActive(getMultiblock().isFormed());
    }

    @Override
    public boolean renderUpdate() {
        return true;
    }
}