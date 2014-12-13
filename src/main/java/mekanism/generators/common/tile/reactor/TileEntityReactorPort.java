package mekanism.generators.common.tile.reactor;

import java.util.EnumSet;

import mekanism.api.gas.Gas;
import mekanism.api.gas.GasRegistry;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.IGasHandler;
import mekanism.api.gas.ITubeConnection;
import mekanism.common.util.CableUtils;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

public class TileEntityReactorPort extends TileEntityReactorBlock implements IFluidHandler, IGasHandler, ITubeConnection
{
	public TileEntityReactorPort()
	{
		super("name", 1);
		
		inventory = new ItemStack[0];
	}

	@Override
	public boolean isFrame()
	{
		return false;
	}

	@Override
	public void onUpdate()
	{
		if(changed)
		{
			worldObj.func_147453_f(xCoord, yCoord, zCoord, getBlockType());
		}
		
		super.onUpdate();

		CableUtils.emit(this);
	}

	@Override
	public int fill(EnumFacing from, FluidStack resource, boolean doFill)
	{
		if(resource.getFluid() == FluidRegistry.WATER && getReactor() != null)
		{
			return getReactor().getWaterTank().fill(resource, doFill);
		}
		
		return 0;
	}

	@Override
	public FluidStack drain(EnumFacing from, FluidStack resource, boolean doDrain)
	{
		if(resource.getFluid() == FluidRegistry.getFluid("steam") && getReactor() != null)
		{
			getReactor().getSteamTank().drain(resource.amount, doDrain);
		}
		
		return null;
	}

	@Override
	public FluidStack drain(EnumFacing from, int maxDrain, boolean doDrain)
	{
		if(getReactor() != null)
		{
			return getReactor().getSteamTank().drain(maxDrain, doDrain);
		}
		
		return null;
	}

	@Override
	public boolean canFill(EnumFacing from, Fluid fluid)
	{
		return (getReactor() != null && fluid == FluidRegistry.WATER);
	}

	@Override
	public boolean canDrain(EnumFacing from, Fluid fluid)
	{
		return (getReactor() != null && fluid == FluidRegistry.WATER);
	}

	@Override
	public FluidTankInfo[] getTankInfo(EnumFacing from)
	{
		if(getReactor() == null)
		{
			return new FluidTankInfo[0];
		}
		
		return new FluidTankInfo[] {getReactor().getWaterTank().getInfo(), getReactor().getSteamTank().getInfo()};
	}

	@Override
	public int receiveGas(EnumFacing side, GasStack stack, boolean doTransfer)
	{
		if(getReactor() != null)
		{
			if(stack.getGas() == GasRegistry.getGas("deuterium"))
			{
				return getReactor().getDeuteriumTank().receive(stack, doTransfer);
			}
			else if(stack.getGas() == GasRegistry.getGas("tritium"))
			{
				return getReactor().getTritiumTank().receive(stack, doTransfer);
			}
			else if(stack.getGas() == GasRegistry.getGas("fusionFuelDT"))
			{
				return getReactor().getFuelTank().receive(stack, doTransfer);
			}
		}
		
		return 0;
	}

	@Override
	public GasStack drawGas(EnumFacing side, int amount, boolean doTransfer)
	{
		if(getReactor() != null)
		{
			if(getReactor().getSteamTank().getFluidAmount() > 0)
			{
				return new GasStack(GasRegistry.getGas("steam"), getReactor().getSteamTank().drain(amount, doTransfer).amount);
			}
		}

		return null;
	}

	@Override
	public boolean canReceiveGas(EnumFacing side, Gas type)
	{
		return (type == GasRegistry.getGas("deuterium") || type == GasRegistry.getGas("tritium") || type == GasRegistry.getGas("fusionFuelDT"));
	}

	@Override
	public boolean canDrawGas(EnumFacing side, Gas type)
	{
		return (type == GasRegistry.getGas("steam"));
	}

	@Override
	public boolean canTubeConnect(EnumFacing side)
	{
		return getReactor() != null;
	}

	@Override
	public boolean canOutputTo(EnumFacing side)
	{
		return true;
	}

	@Override
	public double getEnergy()
	{
		if(getReactor() == null)
		{
			return 0;
		}
		else {
			return getReactor().getBufferedEnergy();
		}
	}

	@Override
	public void setEnergy(double energy)
	{
		if(getReactor() != null)
		{
			getReactor().setBufferedEnergy(energy);
		}
	}

	@Override
	public double getMaxEnergy()
	{
		if(getReactor() == null)
		{
			return 0;
		}
		else {
			return getReactor().getBufferSize();
		}
	}

	@Override
	public EnumSet<EnumFacing> getOutputtingSides()
	{
		EnumSet set = EnumSet.allOf(EnumFacing.class);
		set.remove(EnumFacing.UNKNOWN);
		
		return set;
	}

	@Override
	protected EnumSet<EnumFacing> getConsumingSides()
	{
		return EnumSet.noneOf(EnumFacing.class);
	}

	@Override
	public double getMaxOutput()
	{
		return 1000000000;
	}
}