package mekanism.common.content.boiler;

import mekanism.api.Coord4D;
import mekanism.common.content.boiler.SynchronizedBoilerData.ValveData;
import mekanism.common.tile.TileEntityBoiler;
import mekanism.common.util.MekanismUtils;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidTank;

public abstract class BoilerTank implements IFluidTank
{
	public TileEntityBoiler steamBoiler;

	public BoilerTank(TileEntityBoiler tileEntity)
	{
		steamBoiler = tileEntity;
	}

	public abstract void setFluid(FluidStack stack);

	@Override
	public int getCapacity()
	{
		return steamBoiler.structure != null ? steamBoiler.structure.volume * BoilerUpdateProtocol.WATER_PER_TANK : 0;
	}

	@Override
	public int fill(FluidStack resource, boolean doFill)
	{
		if(steamBoiler.structure != null && !steamBoiler.getWorldObj().isRemote)
		{
			if(resource == null || resource.fluidID <= 0)
			{
				return 0;
			}

			if(getFluid() == null || getFluid().fluidID <= 0)
			{
				if(resource.amount <= getCapacity())
				{
					if(doFill)
					{
						setFluid(resource.copy());
					}

					if(resource.amount > 0 && doFill)
					{
						MekanismUtils.saveChunk(steamBoiler);
						updateValveData(true);
						steamBoiler.sendPacketToRenderer();
						updateValveData(false);
					}

					return resource.amount;
				}
				else {
					if(doFill)
					{
						setFluid(resource.copy());
						getFluid().amount = getCapacity();
					}

					if(getCapacity() > 0 && doFill)
					{
						MekanismUtils.saveChunk(steamBoiler);
						updateValveData(true);
						steamBoiler.sendPacketToRenderer();
						updateValveData(false);
					}

					return getCapacity();
				}
			}

			if(!getFluid().isFluidEqual(resource))
			{
				return 0;
			}

			int space = getCapacity() - getFluid().amount;

			if(resource.amount <= space)
			{
				if(doFill)
				{
					getFluid().amount += resource.amount;
				}

				if(resource.amount > 0 && doFill)
				{
					MekanismUtils.saveChunk(steamBoiler);
					updateValveData(true);
					steamBoiler.sendPacketToRenderer();
					updateValveData(false);
				}

				return resource.amount;
			}
			else {
				if(doFill)
				{
					getFluid().amount = getCapacity();
				}

				if(space > 0 && doFill)
				{
					MekanismUtils.saveChunk(steamBoiler);
					updateValveData(true);
					steamBoiler.sendPacketToRenderer();
					updateValveData(false);
				}

				return space;
			}
		}

		return 0;
	}

	public void updateValveData(boolean value)
	{
		if(steamBoiler.structure != null)
		{
			for(ValveData data : steamBoiler.structure.valves)
			{
				if(data.location.equals(Coord4D.get(steamBoiler)))
				{
					data.serverFluid = value;
				}
			}
		}
	}

	@Override
	public FluidStack drain(int maxDrain, boolean doDrain)
	{
		if(steamBoiler.structure != null && !steamBoiler.getWorldObj().isRemote)
		{
			if(getFluid() == null || getFluid().fluidID <= 0)
			{
				return null;
			}

			if(getFluid().amount <= 0)
			{
				return null;
			}

			int used = maxDrain;

			if(getFluid().amount < used)
			{
				used = getFluid().amount;
			}

			if(doDrain)
			{
				getFluid().amount -= used;
			}

			FluidStack drained = new FluidStack(getFluid().fluidID, used);

			if(getFluid().amount <= 0)
			{
				setFluid(null);
			}

			if(drained.amount > 0 && doDrain)
			{
				MekanismUtils.saveChunk(steamBoiler);
				steamBoiler.sendPacketToRenderer();
			}

			return drained;
		}

		return null;
	}

	@Override
	public int getFluidAmount()
	{
		if(steamBoiler.structure != null)
		{
			return getFluid().amount;
		}

		return 0;
	}

	@Override
	public FluidTankInfo getInfo()
	{
		return new FluidTankInfo(this);
	}
}
