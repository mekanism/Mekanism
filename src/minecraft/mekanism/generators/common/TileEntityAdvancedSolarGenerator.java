package mekanism.generators.common;

import mekanism.common.IBoundingBlock;
import mekanism.common.Mekanism;
import mekanism.common.MekanismUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;

public class TileEntityAdvancedSolarGenerator extends TileEntitySolarGenerator implements IBoundingBlock
{
	public TileEntityAdvancedSolarGenerator()
	{
		super("Advanced Solar Generator", 200000, 512, 240);
	}

	@Override
	public void onPlace() 
	{
		MekanismUtils.makeBoundingBlock(worldObj, xCoord, yCoord+1, zCoord, xCoord, yCoord, zCoord);
		
		for(int x=-1;x<=1;x++)
		{
			for(int z=-1;z<=1;z++)
			{
				MekanismUtils.makeBoundingBlock(worldObj, xCoord+x, yCoord+2, zCoord+z, xCoord, yCoord, zCoord);
			}
		}
	}

	@Override
	public void onBreak() 
	{
		worldObj.func_94571_i(xCoord, yCoord+1, zCoord);
		
		for(int x=-1;x<=1;x++)
		{
			for(int z=-1;z<=1;z++)
			{
				worldObj.func_94571_i(xCoord+x, yCoord+2, zCoord+z);
			}
		}
		
		worldObj.func_94571_i(xCoord, yCoord, zCoord);
	}
}
