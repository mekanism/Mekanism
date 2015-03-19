package mekanism.common.content.tank;

import mekanism.api.Coord4D;
import mekanism.common.content.tank.SynchronizedTankData.ValveData;
import mekanism.common.tile.TileEntityDynamicTank;
import mekanism.common.util.MekanismUtils;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidTank;

public class DynamicFluidTank implements IFluidTank
{
	public TileEntityDynamicTank dynamicTank;

	public DynamicFluidTank(TileEntityDynamicTank tileEntity)
	{
		dynamicTank = tileEntity;
	}

	@Override
	public FluidStack getFluid()
	{
		return dynamicTank.structure != null ? dynamicTank.structure.fluidStored : null;
	}

	@Override
	public int getCapacity()
	{
		return dynamicTank.structure != null ? dynamicTank.structure.volume*TankUpdateProtocol.FLUID_PER_TANK : 0;
	}

	@Override
	public int fill(FluidStack resource, boolean doFill)
	{
		if(dynamicTank.structure != null && !dynamicTank.getWorldObj().isRemote)
		{
			if(resource == null || resource.fluidID <= 0)
			{
				return 0;
			}
			
			if(dynamicTank.structure.fluidStored != null && !dynamicTank.structure.fluidStored.isFluidEqual(resource))
			{
				return 0;
			}

			if(dynamicTank.structure.fluidStored == null || dynamicTank.structure.fluidStored.fluidID <= 0)
			{
				if(resource.amount <= getCapacity())
				{
					if(doFill)
					{
						dynamicTank.structure.fluidStored = resource.copy();
						
						if(resource.amount > 0)
						{
							MekanismUtils.saveChunk(dynamicTank);
							updateValveData(true);
							dynamicTank.sendPacketToRenderer();
							updateValveData(false);
						}
					}

					return resource.amount;
				}
				else {
					if(doFill)
					{
						dynamicTank.structure.fluidStored = resource.copy();
						dynamicTank.structure.fluidStored.amount = getCapacity();
						
						if(getCapacity() > 0)
						{
							MekanismUtils.saveChunk(dynamicTank);
							updateValveData(true);
							dynamicTank.sendPacketToRenderer();
							updateValveData(false);
						}
					}

					return getCapacity();
				}
			}
			else if(resource.amount <= getNeeded())
			{
				if(doFill)
				{
					dynamicTank.structure.fluidStored.amount += resource.amount;
					
					if(resource.amount > 0)
					{
						MekanismUtils.saveChunk(dynamicTank);
						updateValveData(true);
						dynamicTank.sendPacketToRenderer();
						updateValveData(false);
					}
				}

				return resource.amount;
			}
			else {
				if(doFill)
				{
					dynamicTank.structure.fluidStored.amount = getCapacity();
					
					if(getNeeded() > 0)
					{
						MekanismUtils.saveChunk(dynamicTank);
						updateValveData(true);
						dynamicTank.sendPacketToRenderer();
						updateValveData(false);
					}
				}

				return getNeeded();
			}
		}

		return 0;
	}

	public void updateValveData(boolean value)
	{
		if(dynamicTank.structure != null)
		{
			for(ValveData data : dynamicTank.structure.valves)
			{
				if(data.location.equals(Coord4D.get(dynamicTank)))
				{
					data.serverFluid = value;
				}
			}
		}
	}

	@Override
	public FluidStack drain(int maxDrain, boolean doDrain)
	{
		if(dynamicTank.structure != null && !dynamicTank.getWorldObj().isRemote)
		{
			if(dynamicTank.structure.fluidStored == null || dynamicTank.structure.fluidStored.fluidID <= 0)
			{
				return null;
			}

			if(dynamicTank.structure.fluidStored.amount <= 0)
			{
				return null;
			}

			int used = maxDrain;

			if(dynamicTank.structure.fluidStored.amount < used)
			{
				used = dynamicTank.structure.fluidStored.amount;
			}

			if(doDrain)
			{
				dynamicTank.structure.fluidStored.amount -= used;
			}

			FluidStack drained = new FluidStack(dynamicTank.structure.fluidStored.fluidID, used);

			if(dynamicTank.structure.fluidStored.amount <= 0)
			{
				dynamicTank.structure.fluidStored = null;
			}

			if(drained.amount > 0 && doDrain)
			{
				MekanismUtils.saveChunk(dynamicTank);
				dynamicTank.sendPacketToRenderer();
			}

			return drained;
		}

		return null;
	}
	
	public int getNeeded()
	{
		return getCapacity()-getFluidAmount();
	}

	@Override
	public int getFluidAmount()
	{
		if(dynamicTank.structure != null)
		{
			return dynamicTank.structure.fluidStored.amount;
		}

		return 0;
	}

	@Override
	public FluidTankInfo getInfo()
	{
		return new FluidTankInfo(this);
	}
}
