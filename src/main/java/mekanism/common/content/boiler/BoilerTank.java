package mekanism.common.content.boiler;

import mekanism.api.Coord4D;
import mekanism.common.base.MultiblockFluidTank;
import mekanism.common.content.tank.SynchronizedTankData.ValveData;
import mekanism.common.tile.TileEntityBoilerCasing;

public abstract class BoilerTank extends MultiblockFluidTank<TileEntityBoilerCasing> {

    public BoilerTank(TileEntityBoilerCasing tileEntity) {
        super(tileEntity);
    }

    @Override
    protected void updateValveData() {
        if (multiblock.structure != null) {
            Coord4D coord4D = Coord4D.get(multiblock);
            for (ValveData data : multiblock.structure.valves) {
                if (coord4D.equals(data.location)) {
                    data.onTransfer();
                }
            }
        }
    }
}