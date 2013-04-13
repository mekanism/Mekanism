package mekanism.api;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import universalelectricity.core.vector.Vector3;
import universalelectricity.core.vector.VectorHelper;

/**
 * A handy class containing several utilities for efficient gas transfer.
 * @author AidanBrady
 *
 */
public class GasTransmission 
{
    /**
     * Gets all the tubes around a tile entity.
     * @param tileEntity - center tile entity
     * @return array of TileEntities
     */
    public static TileEntity[] getConnectedTubes(TileEntity tileEntity)
    {
    	TileEntity[] tubes = new TileEntity[] {null, null, null, null, null, null};
    	
    	for(ForgeDirection orientation : ForgeDirection.VALID_DIRECTIONS)
    	{
			TileEntity tube = VectorHelper.getTileEntityFromSide(tileEntity.worldObj, new Vector3(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord), orientation);
			
			if(tube instanceof IPressurizedTube && ((IPressurizedTube)tube).canTransferGas(tileEntity))
			{
				tubes[orientation.ordinal()] = tube;
			}
    	}
    	
    	return tubes;
    }
    
    /**
     * Gets all the acceptors around a tile entity.
     * @param tileEntity - center tile entity
     * @return array of IGasAcceptors
     */
    public static IGasAcceptor[] getConnectedAcceptors(TileEntity tileEntity)
    {
    	IGasAcceptor[] acceptors = new IGasAcceptor[] {null, null, null, null, null, null};
    	
    	for(ForgeDirection orientation : ForgeDirection.VALID_DIRECTIONS)
    	{
			TileEntity acceptor = VectorHelper.getTileEntityFromSide(tileEntity.worldObj, new Vector3(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord), orientation);
			
			if(acceptor instanceof IGasAcceptor)
			{
				acceptors[orientation.ordinal()] = (IGasAcceptor)acceptor;
			}
    	}
    	
    	return acceptors;
    }
    
    /**
     * Gets all the tube connections around a tile entity.
     * @param tileEntity - center tile entity
     * @return array of ITubeConnections
     */
    public static ITubeConnection[] getConnections(TileEntity tileEntity)
    {
    	ITubeConnection[] connections = new ITubeConnection[] {null, null, null, null, null, null};
    	
    	for(ForgeDirection orientation : ForgeDirection.VALID_DIRECTIONS)
    	{
			TileEntity connection = VectorHelper.getTileEntityFromSide(tileEntity.worldObj, new Vector3(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord), orientation);
			
			if(connection instanceof ITubeConnection)
			{
				connections[orientation.ordinal()] = (ITubeConnection)connection;
			}
    	}
    	
    	return connections;
    }
    
    /**
     * Emits a defined gas to the network.
     * @param type - gas type to send
     * @param amount - amount of gas to send
     * @param sender - the sender of the gas
     * @param facing - side the sender is outputting from
     * @return rejected gas
     */
    public static int emitGasToNetwork(EnumGas type, int amount, TileEntity sender, ForgeDirection facing)
    {
    	TileEntity pointer = VectorHelper.getTileEntityFromSide(sender.worldObj, new Vector3(sender.xCoord, sender.yCoord, sender.zCoord), facing);
    	
    	if(pointer instanceof IPressurizedTube)
    	{
	    	return new GasTransferProtocol(pointer, sender, type, amount).calculate();
    	}
    	
    	return amount;
    }
    
    /**
     * Emits gas from all sides of a TileEntity.
     * @param type - gas type to send
     * @param amount - amount of gas to send
     * @param pointer - sending TileEntity
     * @return rejected gas
     */
    public static int emitGasFromAllSides(EnumGas type, int amount, TileEntity pointer)
    {
    	if(pointer != null)
    	{
    		return new GasTransferProtocol(pointer, pointer, type, amount).calculate();
    	}
    	
    	return amount;
    }
}
