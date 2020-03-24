package mekanism.common.content.tank;

import mekanism.api.Coord4D;
import mekanism.common.capabilities.fluid.MultiblockFluidTank;
import mekanism.common.content.tank.SynchronizedTankData.ValveData;
import mekanism.common.tile.TileEntityDynamicTank;

public class DynamicFluidTank extends MultiblockFluidTank<TileEntityDynamicTank> {

    public DynamicFluidTank(TileEntityDynamicTank tile) {
        super(tile, () -> tile.structure == null ? 0 : tile.structure.getTankCapacity(), alwaysTrue);
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