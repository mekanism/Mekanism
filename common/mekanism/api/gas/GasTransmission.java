package mekanism.api.gas;

import java.util.HashSet;
import java.util.Set;

import mekanism.api.Object3D;
import mekanism.api.transmitters.ITransmitter;
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
			TileEntity tube = Object3D.get(tileEntity).getFromSide(orientation).getTileEntity(tileEntity.worldObj);
			
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
    public static IGasAcceptor[] getConnectedAcceptors(TileEntity tileEntity)
    {
    	IGasAcceptor[] acceptors = new IGasAcceptor[] {null, null, null, null, null, null};
   
    	for(ForgeDirection orientation : ForgeDirection.VALID_DIRECTIONS)
    	{
			TileEntity acceptor = Object3D.get(tileEntity).getFromSide(orientation).getTileEntity(tileEntity.worldObj);
			
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
			TileEntity connection = Object3D.get(tileEntity).getFromSide(orientation).getTileEntity(tileEntity.worldObj);
			
			if(connection instanceof ITubeConnection && (!(connection instanceof IGasTransmitter) || TransmissionType.checkTransmissionType(connection, TransmissionType.GAS, tileEntity)))
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
     * @return gas sent
     */
    public static int emitGasToNetwork(GasStack stack, TileEntity sender, ForgeDirection facing)
    {
    	TileEntity pointer = Object3D.get(sender).getFromSide(facing).getTileEntity(sender.worldObj);
    	
    	if(TransmissionType.checkTransmissionType(pointer, TransmissionType.GAS, sender))
    	{
	    	return ((ITransmitter<GasNetwork>)pointer).getTransmitterNetwork().emit(stack, sender);
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
