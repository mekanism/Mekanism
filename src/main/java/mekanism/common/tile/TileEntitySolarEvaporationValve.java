package mekanism.common.tile;

import mekanism.api.Coord4D;
import mekanism.common.util.PipeUtils;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

public class TileEntitySolarEvaporationValve extends TileEntitySolarEvaporationBlock implements IFluidHandler
{
	public boolean prevMaster = false;
	
	@Override
	public void onUpdate()
	{
		super.onUpdate();
		
		if(!worldObj.isRemote)
		{
			if((master != null) != prevMaster)
			{
				for(ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
				{
					Coord4D obj = Coord4D.get(this).getFromSide(side);

					if(!obj.isAirBlock(worldObj) && !(obj.getTileEntity(worldObj) instanceof TileEntitySolarEvaporationBlock))
					{
						obj.getBlock(worldObj).onNeighborChange(worldObj, obj.xCoord, obj.yCoord, obj.zCoord, xCoord, yCoord, zCoord);
					}
				}
			}
			
			prevMaster = (master != null);
		}
	}
	
	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill)
	{
		TileEntitySolarEvaporationController controller = getController();
		return controller == null ? 0 : controller.inputTank.fill(resource, doFill);
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain)
	{
		TileEntitySolarEvaporationController controller = getController();
		
		if(controller != null && (resource == null || resource.isFluidEqual(controller.outputTank.getFluid())))
		{
			return controller.outputTank.drain(resource.amount, doDrain);
		}

		return null;
	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain)
	{
		TileEntitySolarEvaporationController controller = getController();
		
		if(controller != null)
		{
			return controller.outputTank.drain(maxDrain, doDrain);
		}

		return null;
	}

	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid)
	{
		TileEntitySolarEvaporationController controller = getController();
		return controller != null && controller.hasRecipe(fluid);
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid)
	{
		TileEntitySolarEvaporationController controller = getController();
		return controller != null && controller.outputTank.getFluidAmount() > 0;
	}

	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from)
	{
		TileEntitySolarEvaporationController controller = getController();
		
		if(controller == null)
		{
			return PipeUtils.EMPTY;
		}

		return new FluidTankInfo[] {new FluidTankInfo(controller.inputTank), new FluidTankInfo(controller.outputTank)};
	}
}
