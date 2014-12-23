package mekanism.common.tile;

import mekanism.api.Coord4D;
import mekanism.common.util.PipeUtils;

import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

public class TileEntitySalinationValve extends TileEntitySalinationBlock implements IFluidHandler
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
				for(EnumFacing side : EnumFacing.values())
				{
					Coord4D obj = Coord4D.get(this).offset(side);

					if(!obj.isAirBlock(worldObj) && !(obj.getTileEntity(worldObj) instanceof TileEntitySalinationBlock))
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
		return master == null ? 0 : master.waterTank.fill(resource, doFill);
	}

	@Override
	public FluidStack drain(EnumFacing from, FluidStack resource, boolean doDrain)
	{
		if(master != null && resource.getFluid() == FluidRegistry.getFluid("brine"))
		{
			return master.brineTank.drain(resource.amount, doDrain);
		}

		return null;
	}

	@Override
	public FluidStack drain(EnumFacing from, int maxDrain, boolean doDrain)
	{
		if(master != null)
		{
			return master.brineTank.drain(maxDrain, doDrain);
		}

		return null;
	}

	@Override
	public boolean canFill(EnumFacing from, Fluid fluid)
	{
		return master != null && fluid == FluidRegistry.getFluid("water");
	}

	@Override
	public boolean canDrain(EnumFacing from, Fluid fluid)
	{
		return master != null && fluid == FluidRegistry.getFluid("brine");
	}

	@Override
	public FluidTankInfo[] getTankInfo(EnumFacing from)
	{
		if(master == null)
		{
			return PipeUtils.EMPTY;
		}

		return new FluidTankInfo[] {new FluidTankInfo(master.waterTank), new FluidTankInfo(master.brineTank)};
	}
}
