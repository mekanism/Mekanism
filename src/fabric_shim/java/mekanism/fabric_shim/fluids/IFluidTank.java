package mekanism.fabric_shim.fluids;

import mekanism.fabric_shim.fluids.capability.IFluidHandler.FluidAction;

/**
 * Single-tank fluid container contract (stand-in for NeoForge's IFluidTank; same surface).
 */
public interface IFluidTank {

    FluidStack getFluid();

    int getFluidAmount();

    int getCapacity();

    boolean isFluidValid(FluidStack stack);

    int fill(FluidStack resource, FluidAction action);

    FluidStack drain(int maxDrain, FluidAction action);

    FluidStack drain(FluidStack resource, FluidAction action);
}
