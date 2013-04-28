package mekanism.generators.common;

import mekanism.api.Object3D;
import mekanism.common.IBoundingBlock;
import mekanism.common.MekanismUtils;

public class TileEntityAdvancedSolarGenerator extends TileEntitySolarGenerator implements IBoundingBlock
{
	public TileEntityAdvancedSolarGenerator()
	{
		super("Advanced Solar Generator", 200000, 480, 240);
	}

	@Override
	public void onPlace() 
	{
		MekanismUtils.makeBoundingBlock(worldObj, xCoord, yCoord+1, zCoord, Object3D.get(this));
		
		for(int x=-1;x<=1;x++)
		{
			for(int z=-1;z<=1;z++)
			{
				MekanismUtils.makeBoundingBlock(worldObj, xCoord+x, yCoord+2, zCoord+z, Object3D.get(this));
			}
		}
	}

	@Override
	public void onBreak() 
	{
		worldObj.setBlockToAir(xCoord, yCoord+1, zCoord);
		
		for(int x=-1;x<=1;x++)
		{
			for(int z=-1;z<=1;z++)
			{
				worldObj.setBlockToAir(xCoord+x, yCoord+2, zCoord+z);
			}
		}
		
		worldObj.setBlockToAir(xCoord, yCoord, zCoord);
	}
}
