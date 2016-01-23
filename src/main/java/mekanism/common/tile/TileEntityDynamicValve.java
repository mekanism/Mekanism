package mekanism.common.tile;

import mekanism.common.content.tank.DynamicFluidTank;
import mekanism.common.util.LangUtils;
import mekanism.common.util.PipeUtils;
import net.minecraft.util.EnumFacing;
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
	public FluidTankInfo[] getTankInfo(EnumFacing from)
	{
		return ((!worldObj.isRemote && structure != null) || (worldObj.isRemote && clientHasStructure)) ? new FluidTankInfo[] {fluidTank.getInfo()} : PipeUtils.EMPTY;
	}

	@Override
	public int fill(EnumFacing from, FluidStack resource, boolean doFill)
	{
		return fluidTank.fill(resource, doFill);
	}

	@Override
	public FluidStack drain(EnumFacing from, FluidStack resource, boolean doDrain)
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
	public FluidStack drain(EnumFacing from, int maxDrain, boolean doDrain)
	{
		if(structure != null)
		{
			return fluidTank.drain(maxDrain, doDrain);
		}

		return null;
	}

	@Override
	public boolean canFill(EnumFacing from, Fluid fluid)
	{
		return ((!worldObj.isRemote && structure != null) || (worldObj.isRemote && clientHasStructure));
	}

	@Override
	public boolean canDrain(EnumFacing from, Fluid fluid)
	{
		return ((!worldObj.isRemote && structure != null) || (worldObj.isRemote && clientHasStructure));
	}
	
	@Override
	public String getInventoryName()
	{
		return LangUtils.localize("gui.dynamicTank");
	}
}
