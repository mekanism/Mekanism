package mekanism.common.tile.component.config.slot;

import java.util.List;
import mekanism.api.fluid.IExtendedFluidTank;

public class FluidSlotInfo extends BaseSlotInfo {

    private final List<IExtendedFluidTank> tanks;

    public FluidSlotInfo(boolean canInput, boolean canOutput, IExtendedFluidTank... tanks) {
        this(canInput, canOutput, List.of(tanks));
    }

    public FluidSlotInfo(boolean canInput, boolean canOutput, List<IExtendedFluidTank> tanks) {
        super(canInput, canOutput);
        this.tanks = tanks;
    }

    public List<IExtendedFluidTank> getTanks() {
        return tanks;
    }

    @Override
    public boolean isEmpty() {
        for (IExtendedFluidTank tank : getTanks()) {
            if (!tank.isEmpty()) {
                return false;
            }
        }
        return true;
    }
}