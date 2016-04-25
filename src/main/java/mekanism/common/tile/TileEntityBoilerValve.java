package mekanism.common.tile;

import mekanism.api.Coord4D;
import mekanism.common.content.boiler.BoilerSteamTank;
import mekanism.common.content.boiler.BoilerTank;
import mekanism.common.content.boiler.BoilerWaterTank;
import mekanism.common.util.PipeUtils;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

public class TileEntityBoilerValve extends TileEntityBoilerCasing implements IFluidHandler
{
	public BoilerTank waterTank;
	public BoilerTank steamTank;

	public TileEntityBoilerValve()
	{
		super("BoilerValve");
		
		waterTank = new BoilerWaterTank(this);
		steamTank = new BoilerSteamTank(this);
	}
	
	@Override
	public void onUpdate()
	{
		super.onUpdate();
		
		if(!worldObj.isRemote)
		{
			if(structure != null && structure.upperRenderLocation != null && getPos().getY() >= structure.upperRenderLocation.getY())
			{
				if(structure.steamStored != null && structure.steamStored.amount > 0)
				{
					for(EnumFacing side : EnumFacing.values())
					{
						TileEntity tile = Coord4D.get(this).offset(side).getTileEntity(worldObj);
						
						if(tile instanceof IFluidHandler && !(tile instanceof TileEntityBoilerValve))
						{
							if(((IFluidHandler)tile).canFill(side.getOpposite(), structure.steamStored.getFluid()))
							{
								structure.steamStored.amount -= ((IFluidHandler)tile).fill(side.getOpposite(), structure.steamStored, true);
								
								if(structure.steamStored.amount <= 0)
								{
									structure.steamStored = null;
								}
							}
						}
					}
				}
			}
		}
	}

	@Override
	public FluidTankInfo[] getTankInfo(EnumFacing from)
	{
		if((!worldObj.isRemote && structure != null) || (worldObj.isRemote && clientHasStructure))
		{
			if(structure.upperRenderLocation != null && getPos().getY() >= structure.upperRenderLocation.getY())
			{
				return new FluidTankInfo[] {steamTank.getInfo()};
			}
			else {
				return new FluidTankInfo[] {waterTank.getInfo()};
			}
		}
		
		return PipeUtils.EMPTY;
	}

	@Override
	public int fill(EnumFacing from, FluidStack resource, boolean doFill)
	{
		if(structure != null && structure.upperRenderLocation != null && getPos().getY() < structure.upperRenderLocation.getY())
		{
			return waterTank.fill(resource, doFill);
		}
		
		return 0;
	}

	@Override
	public FluidStack drain(EnumFacing from, FluidStack resource, boolean doDrain)
	{
		if(structure != null && structure.upperRenderLocation != null && getPos().getY() >= structure.upperRenderLocation.getY())
		{
			if(structure.steamStored != null)
			{
				if(resource.getFluid() == structure.steamStored.getFluid())
				{
					return steamTank.drain(resource.amount, doDrain);
				}
			}
		}

		return null;
	}

	@Override
	public FluidStack drain(EnumFacing from, int maxDrain, boolean doDrain)
	{
		if(structure != null && structure.upperRenderLocation != null && getPos().getY() >= structure.upperRenderLocation.getY())
		{
			return steamTank.drain(maxDrain, doDrain);
		}

		return null;
	}

	@Override
	public boolean canFill(EnumFacing from, Fluid fluid)
	{
		if((!worldObj.isRemote && structure != null) || (worldObj.isRemote && clientHasStructure))
		{
			return structure.upperRenderLocation != null && getPos().getY() < structure.upperRenderLocation.getY();
		}
		
		return false;
	}

	@Override
	public boolean canDrain(EnumFacing from, Fluid fluid)
	{
		if((!worldObj.isRemote && structure != null) || (worldObj.isRemote && clientHasStructure))
		{
			return structure.upperRenderLocation != null && getPos().getY() >= structure.upperRenderLocation.getY();
		}
		
		return false;
	}
}
