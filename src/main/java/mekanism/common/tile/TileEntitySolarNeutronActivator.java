package mekanism.common.tile;

import mekanism.api.Coord4D;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.IGasHandler;
import mekanism.api.gas.ITubeConnection;
import mekanism.common.base.IBoundingBlock;
import mekanism.common.base.ISustainedData;
import mekanism.common.util.MekanismUtils;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;

public class TileEntitySolarNeutronActivator extends TileEntityContainerBlock implements IBoundingBlock, IGasHandler, ITubeConnection, ISustainedData
{
	public TileEntitySolarNeutronActivator()
	{
		super("SolarNeutronActivator");
	}

	@Override
	public void onUpdate() 
	{
		
	}

	@Override
	public void onPlace() 
	{
		MekanismUtils.makeBoundingBlock(worldObj, xCoord, yCoord+1, zCoord, Coord4D.get(this));
	}

	@Override
	public void onBreak() 
	{
		worldObj.setBlockToAir(xCoord, yCoord+1, zCoord);
		worldObj.setBlockToAir(xCoord, yCoord, zCoord);
	}

	@Override
	public int receiveGas(ForgeDirection side, GasStack stack, boolean doTransfer) 
	{
		return 0;
	}

	@Override
	public GasStack drawGas(ForgeDirection side, int amount, boolean doTransfer) 
	{
		return null;
	}

	@Override
	public boolean canReceiveGas(ForgeDirection side, Gas type) 
	{
		return false;
	}

	@Override
	public boolean canDrawGas(ForgeDirection side, Gas type) 
	{
		return false;
	}
	
	@Override
	public boolean canTubeConnect(ForgeDirection side) 
	{
		return side == ForgeDirection.getOrientation(facing) || side == ForgeDirection.DOWN;
	}

	@Override
	public void writeSustainedData(ItemStack itemStack) 
	{
		
	}

	@Override
	public void readSustainedData(ItemStack itemStack) 
	{
		
	}
}
