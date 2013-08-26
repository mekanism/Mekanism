package mekanism.common;

import java.util.Arrays;

import mekanism.api.Object3D;
import mekanism.api.transmitters.ITransmitter;
import mekanism.api.transmitters.TransmissionType;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

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
			
			if(TransmissionType.checkTransmissionType(pipe, TransmissionType.FLUID))
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
		IFluidHandler[] connectedAcceptors = PipeUtils.getConnectedAcceptors(tileEntity);
		
		for(IFluidHandler container : connectedAcceptors)
		{
			if(container != null)
			{
				int side = Arrays.asList(connectedAcceptors).indexOf(container);
				
				if(container.getTankInfo(ForgeDirection.getOrientation(side).getOpposite()) != null && container.getTankInfo(ForgeDirection.getOrientation(side).getOpposite()).length > 0)
				{
					boolean notNull = false;
					
					for(FluidTankInfo info : container.getTankInfo(ForgeDirection.getOrientation(side).getOpposite()))
					{
						if(info != null)
						{
							notNull = true;
							break;
						}
					}
					
					if(notNull)
					{
						connectable[side] = true;
					}
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
     * @return array of IFluidHandlers
     */
    public static IFluidHandler[] getConnectedAcceptors(TileEntity tileEntity)
    {
    	IFluidHandler[] acceptors = new IFluidHandler[] {null, null, null, null, null, null};

    	for(ForgeDirection orientation : ForgeDirection.VALID_DIRECTIONS)
    	{
			TileEntity acceptor = Object3D.get(tileEntity).getFromSide(orientation).getTileEntity(tileEntity.worldObj);
			
			if(acceptor instanceof IFluidHandler && !(acceptor instanceof ITransmitter))
			{
				acceptors[orientation.ordinal()] = (IFluidHandler)acceptor;
			}
    	}
    	
    	return acceptors;
    }
}
