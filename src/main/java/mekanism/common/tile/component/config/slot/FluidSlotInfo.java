package mekanism.common.tile.component.config.slot;

import java.util.Arrays;
import java.util.List;
import mekanism.api.fluid.IExtendedFluidTank;

public class FluidSlotInfo extends BaseSlotInfo {

    //TODO: Should it be a fluid tank instead
    private final List<? extends IExtendedFluidTank> tanks;

    public FluidSlotInfo(boolean canInput, boolean canOutput, IExtendedFluidTank... tanks) {
        this(canInput, canOutput, Arrays.asList(tanks));
    }

    public FluidSlotInfo(boolean canInput, boolean canOutput, List<? extends IExtendedFluidTank> tanks) {
        super(canInput, canOutput);
        this.tanks = tanks;
    }

    public List<? extends IExtendedFluidTank> getTanks() {
        return tanks;
    }
}