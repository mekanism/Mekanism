package mekanism.common.tile;

import mekanism.common.content.tank.DynamicFluidTank;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.PipeUtils;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

public class TileEntityDynamicValve extends TileEntityDynamicTank implements IFluidHandler
{
	public DynamicFluidTank fluidTank;

	public TileEntityDynamicValve()
	{
		super("Dynamic Valve");
		fluidTank = new DynamicFluidTank(this);
	}

	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from)
	{
		return ((!worldObj.isRemote && structure != null) || (worldObj.isRemote && clientHasStructure)) ? new FluidTankInfo[] {fluidTank.getInfo()} : PipeUtils.EMPTY;
	}

	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill)
	{
		return fluidTank.fill(resource, doFill);
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain)
	{
		if(structure != null && structure.fluidStored != null)
		{
			if(resource.getFluid() == structure.fluidStored.getFluid())
			{
				return fluidTank.drain(resource.amount, doDrain);
			}
		}

		return null;
	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain)
	{
		if(structure != null)
		{
			return fluidTank.drain(maxDrain, doDrain);
		}

		return null;
	}

	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid)
	{
		return ((!worldObj.isRemote && structure != null) || (worldObj.isRemote && clientHasStructure));
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid)
	{
		return ((!worldObj.isRemote && structure != null) || (worldObj.isRemote && clientHasStructure));
	}
	
	@Override
	public String getInventoryName()
	{
		return MekanismUtils.localize("gui.dynamicTank");
	}
}
