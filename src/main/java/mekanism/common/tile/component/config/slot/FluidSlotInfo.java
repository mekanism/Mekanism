package mekanism.common.tile.component.config.slot;

import java.util.Arrays;
import java.util.List;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.templates.FluidTank;

public class FluidSlotInfo extends BaseSlotInfo {

    //TODO: Should it be a fluid tank instead
    private final List<FluidTank> tanks;

    public FluidSlotInfo(boolean canInput, boolean canOutput, FluidTank... tanks) {
        this(canInput, canOutput, Arrays.asList(tanks));
    }

    public FluidSlotInfo(boolean canInput, boolean canOutput, List<FluidTank> tanks) {
        super(canInput, canOutput);
        this.tanks = tanks;
    }

    public boolean hasTank(FluidTank tank) {
        //TODO: Test if this even works
        return getTanks().contains(tank);
    }

    public IFluidTank[] getTankInfo() {
        //TODO: check this
        return getTanks().toArray(new IFluidTank[0]);
    }

    public List<FluidTank> getTanks() {
        return tanks;
    }
}