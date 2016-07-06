package mekanism.generators.common.tile.turbine;

import mekanism.api.Coord4D;
import mekanism.common.util.PipeUtils;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

public class TileEntityTurbineVent extends TileEntityTurbineCasing implements IFluidHandler
{
	public FluidTankInfo fakeInfo = new FluidTankInfo(null, 1000);
	
	public TileEntityTurbineVent()
	{
		super("TurbineVent");
	}
	
	@Override
	public void onUpdate()
	{
		super.onUpdate();
		
		if(structure != null && structure.flowRemaining > 0)
		{
			FluidStack fluidStack = new FluidStack(FluidRegistry.WATER, structure.flowRemaining);
			
			for(ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
			{
				TileEntity tile = Coord4D.get(this).getFromSide(side).getTileEntity(worldObj);
				
				if(tile instanceof IFluidHandler)
				{
					if(((IFluidHandler)tile).canFill(side.getOpposite(), fluidStack.getFluid()))
					{
						structure.flowRemaining -= ((IFluidHandler)tile).fill(side.getOpposite(), fluidStack, true);
					}
				}
			}
		}
	}
	
	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from)
	{
		return ((!worldObj.isRemote && structure != null) || (worldObj.isRemote && clientHasStructure)) ? new FluidTankInfo[] {fakeInfo} : PipeUtils.EMPTY;
	}

	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill)
	{
		return 0;
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain)
	{
		return null;
	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain)
	{
		return null;
	}

	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid)
	{
		return false;
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid)
	{
		return fluid == FluidRegistry.WATER;
	}
}
