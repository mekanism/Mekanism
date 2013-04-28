package mekanism.common;

import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.liquids.ILiquidTank;
import net.minecraftforge.liquids.ITankContainer;
import net.minecraftforge.liquids.LiquidStack;

public class TileEntityDynamicValve extends TileEntityDynamicTank implements ITankContainer
{
	public DynamicLiquidTank liquidTank;
	
	public TileEntityDynamicValve()
	{
		super("Dynamic Valve");
		liquidTank = new DynamicLiquidTank(this);
	}

	@Override
	public int fill(ForgeDirection from, LiquidStack resource, boolean doFill)
	{
		return fill(0, resource, doFill);
	}

	@Override
	public int fill(int tankIndex, LiquidStack resource, boolean doFill) 
	{
		if(tankIndex == 0)
		{
			return liquidTank.fill(resource, doFill);
		}
		
		return 0;
	}

	@Override
	public LiquidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) 
	{
		return drain(0, maxDrain, doDrain);
	}

	@Override
	public LiquidStack drain(int tankIndex, int maxDrain, boolean doDrain) 
	{
		if(tankIndex == 0)
		{
			return liquidTank.drain(maxDrain, doDrain);
		}
		
		return null;
	}

	@Override
	public ILiquidTank[] getTanks(ForgeDirection direction)
	{
		if((!worldObj.isRemote && structure != null) || (worldObj.isRemote && clientHasStructure))
		{
			return new ILiquidTank[] {liquidTank};
		}
		
		return null;
	}

	@Override
	public ILiquidTank getTank(ForgeDirection direction, LiquidStack type) 
	{
		if((!worldObj.isRemote && structure != null) || (worldObj.isRemote && clientHasStructure))
		{
			return liquidTank;
		}
		
		return null;
	}
}
