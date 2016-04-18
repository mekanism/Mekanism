package mekanism.common.tile;

import java.util.ArrayList;

import mekanism.api.Coord4D;
import mekanism.api.Range4D;
import mekanism.common.Mekanism;
import mekanism.common.content.boiler.SynchronizedBoilerData;
import mekanism.common.multiblock.TileEntityInternalMultiblock;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.util.MekanismUtils;

public class TileEntitySuperheatingElement extends TileEntityInternalMultiblock 
{
	public boolean prevHot;
	
	@Override
	public void setMultiblock(String id)
	{
		boolean packet = false;
		
		if(id == null && multiblockUUID != null)
		{
			SynchronizedBoilerData.clientHotMap.remove(multiblockUUID);
			packet = true;
		}
		else if(id != null && multiblockUUID == null)
		{
			packet = true;
		}
		
		super.setMultiblock(id);
		
		if(packet && !worldObj.isRemote)
		{
			Mekanism.packetHandler.sendToReceivers(new TileEntityMessage(Coord4D.get(this), getNetworkedData(new ArrayList())), new Range4D(Coord4D.get(this)));
		}
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
