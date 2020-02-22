package mekanism.generators.common.content.turbine;

import mekanism.common.base.MultiblockFluidTank;
import mekanism.generators.common.tile.turbine.TileEntityTurbineCasing;

public class TurbineFluidTank extends MultiblockFluidTank<TileEntityTurbineCasing> {

    public TurbineFluidTank(TileEntityTurbineCasing tile) {
        super(tile);
    }

    @Override
    public int getCapacity() {
        return multiblock.structure == null ? 0 : multiblock.structure.getFluidCapacity();
    }

    @Override
    protected void updateValveData() {
    }
}