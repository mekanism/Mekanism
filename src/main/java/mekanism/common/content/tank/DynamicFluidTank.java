package mekanism.common.content.tank;

import mekanism.api.Coord4D;
import mekanism.common.base.MultiblockFluidTank;
import mekanism.common.content.tank.SynchronizedTankData.ValveData;
import mekanism.common.tile.TileEntityDynamicTank;

public class DynamicFluidTank extends MultiblockFluidTank<TileEntityDynamicTank> {

    public DynamicFluidTank(TileEntityDynamicTank tile) {
        super(tile);
    }

    @Override
    public int getCapacity() {
        return multiblock.structure == null ? 0 : multiblock.structure.volume * TankUpdateProtocol.FLUID_PER_TANK;
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