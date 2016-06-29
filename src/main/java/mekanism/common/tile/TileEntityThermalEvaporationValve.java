package mekanism.common.tile;

import mekanism.api.Coord4D;
import mekanism.api.IHeatTransfer;
import mekanism.common.capabilities.Capabilities;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.FluidTankPropertiesWrapper;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

public class TileEntityThermalEvaporationValve extends TileEntityThermalEvaporationBlock implements IFluidHandler, IHeatTransfer
{
	public boolean prevMaster = false;
	
	@Override
	public void onUpdate()
	{
		super.onUpdate();
		
		if(!worldObj.isRemote)
		{
			if((master == null) == prevMaster)
			{
				for(EnumFacing side : EnumFacing.VALUES)
				{
					Coord4D obj = Coord4D.get(this).offset(side);

					if(!obj.isAirBlock(worldObj) && !(obj.getTileEntity(worldObj) instanceof TileEntityThermalEvaporationBlock))
					{
						obj.getBlock(worldObj).onNeighborChange(worldObj, obj.getPos(), getPos());
					}
				}
			}
			
			prevMaster = (master != null);
		}
	}
	
	@Override
	public int fill(FluidStack resource, boolean doFill)
	{
		return getController().inputTank.fill(resource, doFill);
	}

	@Override
	public FluidStack drain(FluidStack resource, boolean doDrain)
	{
		return getController().outputTank.drain(resource, doDrain);
	}

	@Override
	public FluidStack drain(int maxDrain, boolean doDrain)
	{
		return getController().outputTank.drain(maxDrain, doDrain);
	}

	@Override
	public IFluidTankProperties[] getTankProperties()
	{
		TileEntityThermalEvaporationController controller = getController();
		
		return new IFluidTankProperties[] {new FluidTankPropertiesWrapper(controller.inputTank), new FluidTankPropertiesWrapper(controller.outputTank)};
	}

	@Override
	public double getTemp() 
	{
		return 0;
	}

	@Override
	public double getInverseConductionCoefficient() 
	{
		return 1;
	}

	@Override
	public double getInsulationCoefficient(EnumFacing side)
	{
		return 0;
	}

	@Override
	public void transferHeatTo(double heat) 
	{
		TileEntityThermalEvaporationController controller = getController();
		
		if(controller != null)
		{
			controller.heatToAbsorb += heat;
		}
	}

	@Override
	public double[] simulateHeat() 
	{
		return new double[] {0, 0};
	}

	@Override
	public double applyTemperatureChange() 
	{
		return 0;
	}

	@Override
	public boolean canConnectHeat(EnumFacing side)
	{
		return getController() != null;
	}

	@Override
	public IHeatTransfer getAdjacent(EnumFacing side)
	{
		return null;
	}
	
	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing side)
	{
		return capability == Capabilities.HEAT_TRANSFER_CAPABILITY || 
				(capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && getController() != null) || 
				super.hasCapability(capability, side);
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing side)
	{
		if(capability == Capabilities.HEAT_TRANSFER_CAPABILITY)
		{
			return (T)this;
		}
		
		if(capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && getController() != null)
		{
			return (T)this;
		}
		
		return super.getCapability(capability, side);
	}
}
