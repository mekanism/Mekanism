package mekanism.common.base;

import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

public class FluidHandlerWrapper implements IFluidHandler
{
	public IFluidHandlerWrapper wrapper;
	
	public EnumFacing side;
	
	public FluidHandlerWrapper(IFluidHandlerWrapper w, EnumFacing s)
	{
		wrapper = w;
		side = s;
	}

	@Override
	public IFluidTankProperties[] getTankProperties() 
	{
		return FluidTankProperties.convert(wrapper.getTankInfo(side));
	}

	@Override
	public int fill(FluidStack resource, boolean doFill) 
	{
		return wrapper.fill(side, resource, doFill);
	}

	@Override
	public FluidStack drain(FluidStack resource, boolean doDrain) 
	{
		return wrapper.drain(side, resource, doDrain);
	}

	@Override
	public FluidStack drain(int maxDrain, boolean doDrain) 
	{
		return wrapper.drain(side, maxDrain, doDrain);
	}
}
