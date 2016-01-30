package mekanism.api.gas;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import mekanism.api.Coord4D;
import mekanism.api.transmitters.ITransmitterTile;
import mekanism.api.transmitters.TransmissionType;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

/**
 * A handy class containing several utilities for efficient gas transfer.
 * @author AidanBrady
 *
 */
public final class GasTransmission
{
	public static IGasHandler[] getConnectedAcceptors(TileEntity tileEntity, Collection<EnumFacing> sides)
	{
		IGasHandler[] acceptors = new IGasHandler[] {null, null, null, null, null, null};

		for(EnumFacing orientation : sides)
		{
			TileEntity acceptor = Coord4D.get(tileEntity).offset(orientation).getTileEntity(tileEntity.getWorld());

			if(acceptor instanceof IGasHandler)
			{
				acceptors[orientation.ordinal()] = (IGasHandler)acceptor;
			}
		}

		return acceptors;
	}
	
	/**
	 * Gets all the acceptors around a tile entity.
	 * @param tileEntity - center tile entity
	 * @return array of IGasAcceptors
	 */
	public static IGasHandler[] getConnectedAcceptors(TileEntity tileEntity)
	{
		return getConnectedAcceptors(tileEntity, Arrays.asList(EnumFacing.VALUES));
	}

	/**
	 * Gets all the tube connections around a tile entity.
	 * @param tileEntity - center tile entity
	 * @return array of ITubeConnections
	 */
	public static ITubeConnection[] getConnections(TileEntity tileEntity)
	{
		ITubeConnection[] connections = new ITubeConnection[] {null, null, null, null, null, null};

		for(EnumFacing orientation : EnumFacing.VALUES)
		{
			TileEntity connection = Coord4D.get(tileEntity).offset(orientation).getTileEntity(tileEntity.getWorld());

			if(canConnect(connection, orientation))
			{
				connections[orientation.ordinal()] = (ITubeConnection)connection;
			}
		}

		return connections;
	}

	/**
	 * Whether or not a TileEntity can connect to a specified tile on a specified side.
	 * @param tileEntity - TileEntity to attempt connection to
	 * @param side - side to attempt connection on
	 * @return if this tile and side are connectable
	 */
	public static boolean canConnect(TileEntity tileEntity, EnumFacing side)
	{
		if(tileEntity instanceof ITubeConnection && (!(tileEntity instanceof ITransmitterTile) || TransmissionType.checkTransmissionType(((ITransmitterTile)tileEntity).getTransmitter(), TransmissionType.GAS)))
		{
			if(((ITubeConnection)tileEntity).canTubeConnect(side.getOpposite()))
			{
				return true;
			}
		}

		return false;
	}

	/**
	 * Removes a specified amount of gas from an IGasItem.
	 * @param itemStack - ItemStack of the IGasItem
	 * @param type - type of gas to remove from the IGasItem, null if it doesn't matter
	 * @param amount - amount of gas to remove from the ItemStack
	 * @return the GasStack removed by the IGasItem
	 */
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

	/**
	 * Adds a specified amount of gas to an IGasItem.
	 * @param itemStack - ItemStack of the IGasItem
	 * @param stack - stack to add to the IGasItem
	 * @return amount of gas accepted by the IGasItem
	 */
	public static int addGas(ItemStack itemStack, GasStack stack)
	{
		if(itemStack != null && itemStack.getItem() instanceof IGasItem && ((IGasItem)itemStack.getItem()).canReceiveGas(itemStack, stack.getGas()))
		{
			return ((IGasItem)itemStack.getItem()).addGas(itemStack, stack.copy());
		}

		return 0;
	}
	
	/**
	 * Emits gas from a central block by splitting the received stack among the sides given.
	 * @param stack - the stack to output
	 * @param from - the TileEntity to output from
	 * @param sides - the list of sides to output from
	 * @return the amount of gas emitted
	 */
	public static int emit(GasStack stack, TileEntity from, Collection<EnumFacing> sides)
	{
		if(stack == null)
		{
			return 0;
		}
		
		List<IGasHandler> availableAcceptors = new ArrayList<IGasHandler>();
		IGasHandler[] possibleAcceptors = getConnectedAcceptors(from, sides);
		
		for(int i = 0; i < possibleAcceptors.length; i++)
		{
			IGasHandler handler = possibleAcceptors[i];
			
			if(handler != null && handler.canReceiveGas(EnumFacing.getFront(i).getOpposite(), stack.getGas()))
			{
				availableAcceptors.add(handler);
			}
		}

		Collections.shuffle(availableAcceptors);

		int toSend = stack.amount;
		int prevSending = toSend;

		if(!availableAcceptors.isEmpty())
		{
			int divider = availableAcceptors.size();
			int remaining = toSend % divider;
			int sending = (toSend-remaining)/divider;

			for(IGasHandler acceptor : availableAcceptors)
			{
				int currentSending = sending;

				if(remaining > 0)
				{
					currentSending++;
					remaining--;
				}
				
				EnumFacing dir = EnumFacing.getFront(Arrays.asList(possibleAcceptors).indexOf(acceptor)).getOpposite();
				toSend -= acceptor.receiveGas(dir, new GasStack(stack.getGas(), currentSending), true);
			}
		}

		return prevSending-toSend;
	}
}
