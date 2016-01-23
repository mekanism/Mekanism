package mekanism.generators.common.tile.turbine;

import mekanism.common.util.LangUtils;
import mekanism.common.util.PipeUtils;
import mekanism.generators.common.content.turbine.TurbineFluidTank;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

public class TileEntityTurbineValve extends TileEntityTurbineCasing implements IFluidHandler
{
	public TurbineFluidTank fluidTank;

	public TileEntityTurbineValve()
	{
		super("TurbineValve");
		fluidTank = new TurbineFluidTank(this);
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
		if(fluid == FluidRegistry.getFluid("steam"))
		{
			return ((!worldObj.isRemote && structure != null) || (worldObj.isRemote && clientHasStructure));
		}
		
		return false;
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid)
	{
		return ((!worldObj.isRemote && structure != null) || (worldObj.isRemote && clientHasStructure));
	}
	
	@Override
	public String getInventoryName()
	{
		return LangUtils.localize("gui.industrialTurbine");
	}
}
