package mekanism.common.tile;

import mekanism.api.Coord4D;
import mekanism.common.util.PipeUtils;
import net.minecraft.util.EnumFacing;
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
			if((master == null) == prevMaster)
			{
				for(EnumFacing side : EnumFacing.VALUES)
				{
					Coord4D obj = Coord4D.get(this).offset(side);

					if(!obj.isAirBlock(worldObj) && !(obj.getTileEntity(worldObj) instanceof TileEntitySolarEvaporationBlock))
					{
						obj.getBlock(worldObj).onNeighborChange(worldObj, obj, getPos());
					}
				}
			}
			
			prevMaster = (master != null);
		}
	}
	
	@Override
	public int fill(EnumFacing from, FluidStack resource, boolean doFill)
	{
		TileEntitySolarEvaporationController controller = getController();
		return controller == null ? 0 : controller.inputTank.fill(resource, doFill);
	}

	@Override
	public FluidStack drain(EnumFacing from, FluidStack resource, boolean doDrain)
	{
		TileEntitySolarEvaporationController controller = getController();
		
		if(controller != null && (resource == null || resource.isFluidEqual(controller.outputTank.getFluid())))
		{
			return controller.outputTank.drain(resource.amount, doDrain);
		}

		return null;
	}

	@Override
	public FluidStack drain(EnumFacing from, int maxDrain, boolean doDrain)
	{
		TileEntitySolarEvaporationController controller = getController();
		
		if(controller != null)
		{
			return controller.outputTank.drain(maxDrain, doDrain);
		}

		return null;
	}

	@Override
	public boolean canFill(EnumFacing from, Fluid fluid)
	{
		TileEntitySolarEvaporationController controller = getController();
		return controller != null && controller.hasRecipe(fluid);
	}

	@Override
	public boolean canDrain(EnumFacing from, Fluid fluid)
	{
		TileEntitySolarEvaporationController controller = getController();
		return controller != null && controller.outputTank.getFluidAmount() > 0;
	}

	@Override
	public FluidTankInfo[] getTankInfo(EnumFacing from)
	{
		TileEntitySolarEvaporationController controller = getController();
		
		if(controller == null)
		{
			return PipeUtils.EMPTY;
		}

		return new FluidTankInfo[] {new FluidTankInfo(controller.inputTank), new FluidTankInfo(controller.outputTank)};
	}
}
