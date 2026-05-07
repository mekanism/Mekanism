package mekanism.common.tile.component.config.slot;

import java.util.List;
import mekanism.api.fluid.IFluidTank;

public class FluidSlotInfo extends BaseSlotInfo {

    private final List<IFluidTank> tanks;

    public FluidSlotInfo(boolean canInput, boolean canOutput, IFluidTank... tanks) {
        this(canInput, canOutput, List.of(tanks));
    }

    public FluidSlotInfo(boolean canInput, boolean canOutput, List<IFluidTank> tanks) {
        super(canInput, canOutput);
        this.tanks = tanks;
    }

    public List<IFluidTank> getTanks() {
        return tanks;
    }

    @Override
    public boolean isEmpty() {
        for (IFluidTank tank : getTanks()) {
            if (!tank.isEmpty()) {
                return false;
            }
        }
        return true;
    }
}