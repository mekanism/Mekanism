package mekanism.common.tile;

import mekanism.common.content.tank.DynamicFluidTank;
import mekanism.common.util.LangUtils;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

public class TileEntityDynamicValve extends TileEntityDynamicTank implements IFluidHandler
{
	public DynamicFluidTank fluidTank;

	public TileEntityDynamicValve()
	{
		super("Dynamic Valve");
		fluidTank = new DynamicFluidTank(this);
	}

	@Override
	public IFluidTankProperties[] getTankProperties()
	{
		return new IFluidTankProperties[] {fluidTank};
	}

	@Override
	public int fill(FluidStack resource, boolean doFill)
	{
		return fluidTank.fill(resource, doFill);
	}

	@Override
	public FluidStack drain(FluidStack resource, boolean doDrain)
	{
		if(structure.fluidStored != null)
		{
			if(resource.getFluid() == structure.fluidStored.getFluid())
			{
				return fluidTank.drain(resource.amount, doDrain);
			}
		}

		return null;
	}

	@Override
	public FluidStack drain(int maxDrain, boolean doDrain)
	{
		return fluidTank.drain(maxDrain, doDrain);
	}
	
	@Override
	public String getName()
	{
		return LangUtils.localize("gui.dynamicTank");
	}
	
	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing side)
	{
		if((!worldObj.isRemote && structure != null) || (worldObj.isRemote && clientHasStructure))
		{
			if(capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
			{
				return true;
			}
		}
		
		return super.hasCapability(capability, side);
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing side)
	{
		if((!worldObj.isRemote && structure != null) || (worldObj.isRemote && clientHasStructure))
		{
			if(capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
			{
				return (T)this;
			}
		}
		
		return super.getCapability(capability, side);
	}
}
