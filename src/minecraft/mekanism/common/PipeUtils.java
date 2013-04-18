package mekanism.common;

import mekanism.api.IMechanicalPipe;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.liquids.ITankContainer;
import universalelectricity.core.vector.Vector3;
import universalelectricity.core.vector.VectorHelper;

public final class PipeUtils 
{
    /**
     * Gets all the pipes around a tile entity.
     * @param tileEntity - center tile entity
     * @return array of TileEntities
     */
    public static TileEntity[] getConnectedPipes(TileEntity tileEntity)
    {
    	TileEntity[] tubes = new TileEntity[] {null, null, null, null, null, null};
    	
    	for(ForgeDirection orientation : ForgeDirection.VALID_DIRECTIONS)
    	{
			TileEntity tube = VectorHelper.getTileEntityFromSide(tileEntity.worldObj, new Vector3(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord), orientation);
			
			if(tube instanceof IMechanicalPipe && ((IMechanicalPipe)tube).canTransferLiquids(tileEntity))
			{
				tubes[orientation.ordinal()] = tube;
			}
    	}
    	
    	return tubes;
    }
    
    /**
     * Gets all the acceptors around a tile entity.
     * @param tileEntity - center tile entity
     * @return array of ITankContainers
     */
    public static ITankContainer[] getConnectedAcceptors(TileEntity tileEntity)
    {
    	ITankContainer[] acceptors = new ITankContainer[] {null, null, null, null, null, null};
    	
    	for(ForgeDirection orientation : ForgeDirection.VALID_DIRECTIONS)
    	{
			TileEntity acceptor = VectorHelper.getTileEntityFromSide(tileEntity.worldObj, new Vector3(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord), orientation);
			
			if(acceptor instanceof ITankContainer)
			{
				acceptors[orientation.ordinal()] = (ITankContainer)acceptor;
			}
    	}
    	
    	return acceptors;
    }
}
