package buildcraft.api.transport.pipe;

import net.minecraft.util.EnumFacing;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import buildcraft.api.core.IFluidFilter;

public interface IFlowFluid {
    /** @param millibuckets
     * @param from
     * @param filter The fluidstack that the extracted fluid must match, or null for any fluid.
     * @return The fluidstack extracted and inserted into the pipe. */
    FluidStack tryExtractFluid(int millibuckets, EnumFacing from, FluidStack filter);

    /** Advanced version of {@link #tryExtractFluid(int, EnumFacing, FluidStack)}. Note that this only works for
     * instances of {@link IFluidHandler} that ALSO extends IFluidHandlerAdv
     * 
     * @param millibuckets
     * @param from
     * @param filter A filter to try and match fluids.
     * @return The fluidstack extracted and inserted into the pipe. */
    FluidStack tryExtractFluidAdv(int millibuckets, EnumFacing from, IFluidFilter filter);
}
