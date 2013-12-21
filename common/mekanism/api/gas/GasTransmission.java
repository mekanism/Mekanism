package mekanism.api.gas;

import mekanism.api.Coord4D;
import mekanism.api.transmitters.IGridTransmitter;
import mekanism.api.transmitters.TransmissionType;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;

/**
 * A handy class containing several utilities for efficient gas transfer.
 * @author AidanBrady
 *
 */
public final class GasTransmission 
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
			TileEntity tube = Coord4D.get(tileEntity).getFromSide(orientation).getTileEntity(tileEntity.worldObj);
			
			if(TransmissionType.checkTransmissionType(tube, TransmissionType.GAS, tileEntity))
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
    public static IGasHandler[] getConnectedAcceptors(TileEntity tileEntity)
    {
    	IGasHandler[] acceptors = new IGasHandler[] {null, null, null, null, null, null};
   
    	for(ForgeDirection orientation : ForgeDirection.VALID_DIRECTIONS)
    	{
			TileEntity acceptor = Coord4D.get(tileEntity).getFromSide(orientation).getTileEntity(tileEntity.worldObj);
			
			if(acceptor instanceof IGasHandler)
			{
				acceptors[orientation.ordinal()] = (IGasHandler)acceptor;
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
			TileEntity connection = Coord4D.get(tileEntity).getFromSide(orientation).getTileEntity(tileEntity.worldObj);
			
			if(canConnect(connection, orientation))
			{
				connections[orientation.ordinal()] = (ITubeConnection)connection;
			}
    	}
    	
    	return connections;
    }
    
    public static boolean canConnect(TileEntity tileEntity, ForgeDirection side)
    {
		if(tileEntity instanceof ITubeConnection && (!(tileEntity instanceof IGasTransmitter) || TransmissionType.checkTransmissionType(tileEntity, TransmissionType.GAS, tileEntity)))
		{
			if(((ITubeConnection)tileEntity).canTubeConnect(side.getOpposite()))
			{
				return true;
			}
		}
		
		return false;
    }
    
    /**
     * Emits a defined gas to the network.
     * @param type - gas type to send
     * @param amount - amount of gas to send
     * @param sender - the sender of the gas
     * @param facing - side the sender is outputting from
     * @return gas sent
     */
    public static int emitGasToNetwork(GasStack stack, TileEntity sender, ForgeDirection facing)
    {
    	TileEntity pointer = Coord4D.get(sender).getFromSide(facing).getTileEntity(sender.worldObj);
    	
    	if(TransmissionType.checkTransmissionType(pointer, TransmissionType.GAS, sender))
    	{
	    	return ((IGridTransmitter<GasNetwork>)pointer).getTransmitterNetwork().emit(stack);
    	}
    	
    	return 0;
    }

	public static GasStack removeGas(ItemStack itemStack, Gas type, int amount)
	{
		if(itemStack != null && itemStack.getItem() instanceof IGasItem)
		{
			IGasItem item = (IGasItem)itemStack.getItem();
			
			if(type != null && item.getGas(itemStack) != null && item.getGas(itemStack).getGas() != type || !item.canProvideGas(itemStack, type))
			{
				return null;
			}
			
			return item.removeGas(itemStack, amount);
		}
		
		return null;
	}

	public static int addGas(ItemStack itemStack, GasStack stack)
	{
		if(itemStack != null && itemStack.getItem() instanceof IGasItem && ((IGasItem)itemStack.getItem()).canReceiveGas(itemStack, stack.getGas()))
		{
			return ((IGasItem)itemStack.getItem()).addGas(itemStack, stack.copy());
		}
		
		return 0;
	}
}
