package mekanism.common.tile;

import mekanism.common.content.boiler.SynchronizedBoilerData;
import mekanism.common.multiblock.TileEntityInternalMultiblock;
import mekanism.common.util.MekanismUtils;

public class TileEntitySuperheatingElement extends TileEntityInternalMultiblock 
{
	public boolean prevHot;
	
	@Override
	public void setMultiblock(String id)
	{
		if(id == null && multiblockUUID != null)
		{
			SynchronizedBoilerData.clientHotMap.remove(multiblockUUID);
		}
		
		super.setMultiblock(id);
	}
	
	@Override
	public boolean canUpdate()
	{
		return true;
	}
	
	@Override
	public void onUpdate()
	{
		if(worldObj.isRemote)
		{
			boolean newHot = false;
			
			if(multiblockUUID != null && SynchronizedBoilerData.clientHotMap.get(multiblockUUID) != null)
			{
				newHot = SynchronizedBoilerData.clientHotMap.get(multiblockUUID);
			}
			
			if(prevHot != newHot)
			{
				MekanismUtils.updateBlock(worldObj, xCoord, yCoord, zCoord);
				
				prevHot = newHot;
			}
		}
	}
}
