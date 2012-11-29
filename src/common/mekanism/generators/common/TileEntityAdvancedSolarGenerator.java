package mekanism.generators.common;

import universalelectricity.core.vector.Vector3;
import universalelectricity.prefab.multiblock.*;
import mekanism.common.Mekanism;
import mekanism.generators.common.BlockGenerator.GeneratorType;
import net.minecraft.src.*;

public class TileEntityAdvancedSolarGenerator extends TileEntitySolarGenerator implements IMultiBlock
{
	public TileEntityAdvancedSolarGenerator()
	{
		super("Advanced Solar Generator", 600000, 256, 256);
	}

	@Override
	public void onCreate(Vector3 position) 
	{
		Mekanism.NullRender.makeFakeBlock(worldObj, new Vector3(xCoord, yCoord+1, zCoord), new Vector3(xCoord, yCoord, zCoord));
		
		for(int x=-1;x<2;x++)
		{
			for(int z=-1;z<2;z++)
			{
				Mekanism.NullRender.makeFakeBlock(worldObj, new Vector3(xCoord+x, yCoord+2, zCoord+z), position);
			}
		}
	}

	@Override
	public void onDestroy(TileEntity tileEntity) 
	{
		worldObj.setBlock(xCoord, yCoord+1, zCoord, 0);
		
		for(int x=-1;x<2;x++)
		{
			for(int z=-1;z<2;z++)
			{
				worldObj.setBlock(xCoord+x, yCoord+2, zCoord+z, 0);
			}
		}
		
		worldObj.setBlock(xCoord, yCoord, zCoord, 0);
		invalidate();
	}

	@Override
	public boolean onActivated(EntityPlayer entityplayer) 
	{
    	if(!entityplayer.isSneaking())
    	{
    		entityplayer.openGui(MekanismGenerators.instance, 1, worldObj, xCoord, yCoord, zCoord);
    		return true;
    	}
        return false;
	}
}
