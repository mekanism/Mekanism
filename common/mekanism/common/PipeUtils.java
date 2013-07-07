package mekanism.common;

import java.util.Arrays;

import mekanism.api.Object3D;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.liquids.ITankContainer;
import net.minecraftforge.liquids.LiquidStack;

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
			
			if(pipe instanceof IMechanicalPipe)
			{
				pipes[orientation.ordinal()] = pipe;
			}
    	}
    	
    	return pipes;
    }

    /**
     * Gets all the adjacent connections to a TileEntity.
     * @param tileEntity - center TileEntity
     * @return boolean[] of adjacent connections
     */
    public static boolean[] getConnections(TileEntity tileEntity)
    {
		boolean[] connectable = new boolean[] {false, false, false, false, false, false};
		
		TileEntity[] connectedPipes = PipeUtils.getConnectedPipes(tileEntity);
		ITankContainer[] connectedAcceptors = PipeUtils.getConnectedAcceptors(tileEntity);
		
		for(ITankContainer container : connectedAcceptors)
		{
			if(container != null)
			{
				int side = Arrays.asList(connectedAcceptors).indexOf(container);
				
				if(container.getTanks(ForgeDirection.getOrientation(side).getOpposite()) != null && container.getTanks(ForgeDirection.getOrientation(side).getOpposite()).length != 0)
				{
					connectable[side] = true;
				}
				else if(container.getTank(ForgeDirection.getOrientation(side).getOpposite(), new LiquidStack(-1, 1000)) != null)
				{
					connectable[side] = true;
				}
			}
		}
		
		for(TileEntity tile : connectedPipes)
		{
			if(tile != null)
			{
				int side = Arrays.asList(connectedPipes).indexOf(tile);
				
				connectable[side] = true;
			}
		}
		
		return connectable;
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
			
			if(acceptor instanceof ITankContainer && !(acceptor instanceof IMechanicalPipe))
			{
				acceptors[orientation.ordinal()] = (ITankContainer)acceptor;
			}
    	}
    	
    	return acceptors;
    }
}
