package mekanism.common.tile;

import mekanism.api.Coord4D;
import mekanism.common.util.PipeUtils;

import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

public class TileEntitySalinationValve extends TileEntitySalinationTank implements IFluidHandler
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

					if(!obj.isAirBlock(worldObj) && !(obj.getTileEntity(worldObj) instanceof TileEntitySalinationTank))
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
		return master == null ? 0 : master.waterTank.fill(resource, doFill);
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain)
	{
		if(master != null && resource.getFluid() == FluidRegistry.getFluid("brine"))
		{
			return master.brineTank.drain(resource.amount, doDrain);
		}

		return null;
	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain)
	{
		if(master != null)
		{
			return master.brineTank.drain(maxDrain, doDrain);
		}

		return null;
	}

	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid)
	{
		return master != null && fluid == FluidRegistry.getFluid("water");
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid)
	{
		return master != null && fluid == FluidRegistry.getFluid("brine");
	}

	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from)
	{
		if(master == null)
		{
			return PipeUtils.EMPTY;
		}

		return new FluidTankInfo[] {new FluidTankInfo(master.waterTank), new FluidTankInfo(master.brineTank)};
	}
}
