package mekanism.common.tile;

import java.util.ArrayList;
import java.util.EnumSet;

import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.IGasHandler;
import mekanism.api.gas.ITubeConnection;
import mekanism.common.teleportation.SharedInventory;
import mekanism.common.teleportation.SharedInventoryManager;
import mekanism.common.util.CableUtils;

import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

public class TileEntityEntangledInventory extends TileEntityElectricBlock implements IFluidHandler, IGasHandler, ITubeConnection
{
	public SharedInventory sharedInventory;

	public TileEntityEntangledInventory()
	{
		super("Entangled", 0);
		inventory = new ItemStack[0];
	}

	@Override
	public void onUpdate()
	{
		super.onUpdate();

		CableUtils.emit(this);
	}

	public void setInventory(String frequency)
	{
		sharedInventory = SharedInventoryManager.getInventory(frequency);
	}

	public EnumSet<ForgeDirection> getOutputtingSides()
	{
		return EnumSet.of(ForgeDirection.UP);
	}

	protected EnumSet<ForgeDirection> getConsumingSides()
	{
		EnumSet set = EnumSet.allOf(ForgeDirection.class);
		set.remove(ForgeDirection.UNKNOWN);
		set.remove(ForgeDirection.UP);
		return set;
	}

	public double getMaxOutput()
	{
		return sharedInventory == null ? 0 : 1000;
	}

	@Override
	public double getEnergy()
	{
		return sharedInventory == null ? 0 : sharedInventory.getEnergy();
	}

	@Override
	public void setEnergy(double energy)
	{
		if(sharedInventory != null)
		{
			sharedInventory.setEnergy(energy);
		}
	}

	@Override
	public double getMaxEnergy()
	{
		return sharedInventory == null ? 0 : sharedInventory.getMaxEnergy();
	}

	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill)
	{
		return sharedInventory == null ? 0 : sharedInventory.fill(from, resource, doFill);
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain)
	{
		if(sharedInventory == null)
		{
			return null;
		}
		return sharedInventory.drain(from, resource, doDrain);
	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain)
	{
		if(sharedInventory == null)
		{
			return null;
		}
		return sharedInventory.drain(from, maxDrain, doDrain);
	}

	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid)
	{
		return sharedInventory == null ? false : sharedInventory.canFill(from, fluid);
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid)
	{
		return sharedInventory == null ? false : sharedInventory.canDrain(from, fluid);
	}

	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from)
	{
		return sharedInventory == null ? new FluidTankInfo[0] : sharedInventory.getTankInfo(from);
	}

	@Override
	public int receiveGas(ForgeDirection side, GasStack stack)
	{
		return sharedInventory == null ? 0 : sharedInventory.receiveGas(side, stack);
	}

	@Override
	public GasStack drawGas(ForgeDirection side, int amount)
	{
		return sharedInventory == null ? null : sharedInventory.drawGas(side, amount);
	}

	@Override
	public boolean canReceiveGas(ForgeDirection side, Gas type)
	{
		return sharedInventory == null ? false : sharedInventory.canReceiveGas(side, type);
	}

	@Override
	public boolean canDrawGas(ForgeDirection side, Gas type)
	{
		return sharedInventory == null ? false : sharedInventory.canDrawGas(side, type);
	}

	@Override
	public boolean canTubeConnect(ForgeDirection side)
	{
		return sharedInventory == null ? false : true;
	}
}
