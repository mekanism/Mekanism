package mekanism.common.tile;

import mekanism.api.Coord4D;
import mekanism.api.IHeatTransfer;
import mekanism.common.util.PipeUtils;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

public class TileEntityThermalEvaporationValve extends TileEntityThermalEvaporationBlock implements IFluidHandler, IHeatTransfer
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

					if(!obj.isAirBlock(worldObj) && !(obj.getTileEntity(worldObj) instanceof TileEntityThermalEvaporationBlock))
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
		TileEntityThermalEvaporationController controller = getController();
		return controller == null ? 0 : controller.inputTank.fill(resource, doFill);
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain)
	{
		TileEntityThermalEvaporationController controller = getController();
		
		if(controller != null && (resource == null || resource.isFluidEqual(controller.outputTank.getFluid())))
		{
			return controller.outputTank.drain(resource.amount, doDrain);
		}

		return null;
	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain)
	{
		TileEntityThermalEvaporationController controller = getController();
		
		if(controller != null)
		{
			return controller.outputTank.drain(maxDrain, doDrain);
		}

		return null;
	}

	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid)
	{
		TileEntityThermalEvaporationController controller = getController();
		return controller != null && controller.hasRecipe(fluid);
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid)
	{
		TileEntityThermalEvaporationController controller = getController();
		return controller != null && controller.outputTank.getFluidAmount() > 0;
	}

	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from)
	{
		TileEntityThermalEvaporationController controller = getController();
		
		if(controller == null)
		{
			return PipeUtils.EMPTY;
		}

		return new FluidTankInfo[] {new FluidTankInfo(controller.inputTank), new FluidTankInfo(controller.outputTank)};
	}

	@Override
	public double getTemp() 
	{
		return 0;
	}

	@Override
	public double getInverseConductionCoefficient() 
	{
		return 1;
	}

	@Override
	public double getInsulationCoefficient(ForgeDirection side) 
	{
		return 0;
	}

	@Override
	public void transferHeatTo(double heat) 
	{
		TileEntityThermalEvaporationController controller = getController();
		
		if(controller != null)
		{
			controller.heatToAbsorb += heat;
		}
	}

	@Override
	public double[] simulateHeat() 
	{
		return new double[] {0, 0};
	}

	@Override
	public double applyTemperatureChange() 
	{
		return 0;
	}

	@Override
	public boolean canConnectHeat(ForgeDirection side) 
	{
		return getController() != null;
	}

	@Override
	public IHeatTransfer getAdjacent(ForgeDirection side) 
	{
		return null;
	}
}
