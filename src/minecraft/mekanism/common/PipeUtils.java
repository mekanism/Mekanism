package mekanism.common;

import mekanism.api.IMechanicalPipe;
import mekanism.api.Object3D;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.liquids.ITankContainer;

public final class PipeUtils 
{
    /**
     * Gets all the pipes around a tile entity.
     * @param tileEntity - center tile entity
     * @return array of TileEntities
     */
    public static TileEntity[] getConnectedPipes(TileEntity tileEntity)
    {
    	TileEntity[] pipes = new TileEntity[] {null, null, null, null, null, null};
    	
    	for(ForgeDirection orientation : ForgeDirection.VALID_DIRECTIONS)
    	{
			TileEntity pipe = Object3D.get(tileEntity).getFromSide(orientation).getTileEntity(tileEntity.worldObj);
			
			if(pipe instanceof IMechanicalPipe && ((IMechanicalPipe)pipe).canTransferLiquids(tileEntity))
			{
				pipes[orientation.ordinal()] = pipe;
			}
    	}
    	
    	return pipes;
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
			TileEntity acceptor = Object3D.get(tileEntity).getFromSide(orientation).getTileEntity(tileEntity.worldObj);
			
			if(acceptor instanceof ITankContainer)
			{
				acceptors[orientation.ordinal()] = (ITankContainer)acceptor;
			}
    	}
    	
    	return acceptors;
    }
}
