package mekanism.common.tile.component.config.slot;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.templates.FluidTank;

public class FluidSlotInfo implements ISlotInfo {

    //TODO: Should it be a fluid tank instead
    private final List<FluidTank> tanks;

    public FluidSlotInfo() {
        tanks = Collections.emptyList();
    }

    public FluidSlotInfo(FluidTank... tanks) {
        this(Arrays.asList(tanks));
    }

    public FluidSlotInfo(List<FluidTank> tanks) {
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