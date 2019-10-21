package mekanism.common.tile.component.config.slot;

import java.util.Arrays;
import java.util.List;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class FluidSlotInfo implements ISlotInfo {

    //TODO: Should it be a fluid tank instead
    private final List<IFluidHandler> handlers;

    public FluidSlotInfo(IFluidHandler... handlers) {
        this(Arrays.asList(handlers));
    }

    public FluidSlotInfo(List<IFluidHandler> handlers) {
        this.handlers = handlers;
    }

    public boolean hasTank(IFluidHandler tank) {
        //TODO: Test if this even works
        return handlers.contains(tank);
    }

    public IFluidTank[] getTankInfo() {
        //TODO
        return new IFluidTank[0];
    }
}