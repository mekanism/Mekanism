package mekanism.generators.common.tile.reactor;

import mekanism.api.gas.Gas;
import mekanism.api.gas.GasRegistry;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.IGasHandler;

import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

public class TileEntityReactorPort extends TileEntityReactorBlock implements IFluidHandler, IGasHandler
{
	@Override
	public boolean isFrame()
	{
		return false;
	}

	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill)
	{
		if(resource.getFluid() == FluidRegistry.WATER && getReactor() != null)
		{
			return getReactor().getWaterTank().fill(resource, doFill);
		}
		return 0;
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain)
	{
		if(resource.getFluid() == FluidRegistry.getFluid("steam") && getReactor() != null)
		{
			getReactor().getSteamTank().drain(resource.amount, doDrain);
		}
		return null;
	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain)
	{
		if(getReactor() != null)
		{
			getReactor().getSteamTank().drain(maxDrain, doDrain);
		}
		return null;
	}

	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid)
	{
		return (getReactor() != null && fluid == FluidRegistry.WATER);
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid)
	{
		return (getReactor() != null && fluid == FluidRegistry.WATER);
	}

	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from)
	{
		if(getReactor() == null)
		{
			return new FluidTankInfo[0];
		}
		return new FluidTankInfo[] {getReactor().getWaterTank().getInfo(), getReactor().getSteamTank().getInfo()};
	}

	@Override
	public int receiveGas(ForgeDirection side, GasStack stack)
	{
		if(getReactor() != null)
		{
			if(stack.getGas() == GasRegistry.getGas("deuterium"))
			{
				return getReactor().getDeuteriumTank().receive(stack, true);
			}
			else if(stack.getGas() == GasRegistry.getGas("tritium"))
			{
				return getReactor().getTritiumTank().receive(stack, true);
			}
			else if(stack.getGas() == GasRegistry.getGas("fusionFuel"))
			{
				return getReactor().getFuelTank().receive(stack, true);
			}
		}
		return 0;
	}

	@Override
	public GasStack drawGas(ForgeDirection side, int amount)
	{
		if(getReactor() != null)
		{
			if(getReactor().getSteamTank().getFluidAmount() > 0)
			{
				return new GasStack(GasRegistry.getGas("steam"), getReactor().getSteamTank().drain(amount, true).amount);
			}
		}

		return null;
	}

	@Override
	public boolean canReceiveGas(ForgeDirection side, Gas type)
	{
		return (type == GasRegistry.getGas("deuterium") || type == GasRegistry.getGas("tritium") || type == GasRegistry.getGas("fusionFuel"));
	}

	@Override
	public boolean canDrawGas(ForgeDirection side, Gas type)
	{
		return (type == GasRegistry.getGas("steam"));
	}
}