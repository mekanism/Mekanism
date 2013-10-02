package mekanism.common;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import mekanism.api.transmitters.DynamicNetwork;
import mekanism.api.transmitters.ITransmitter;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.util.TransporterUtils;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;

public class InventoryNetwork extends DynamicNetwork<IInventory, InventoryNetwork, ItemStack>
{
	public InventoryNetwork(ITransmitter<InventoryNetwork, ItemStack>... varTransporters)
	{
		transmitters.addAll(Arrays.asList(varTransporters));
		register();
	}
	
	public InventoryNetwork(Collection<ITransmitter<InventoryNetwork, ItemStack>> collection)
	{
		transmitters.addAll(collection);
		register();
	}
	
	public InventoryNetwork(Set<InventoryNetwork> networks)
	{
		for(InventoryNetwork net : networks)
		{
			if(net != null)
			{
				addAllTransmitters(net.transmitters);
				net.deregister();
			}
		}
		
		refresh();
		register();
	}
	
	@Override
	public Set<IInventory> getAcceptors(Object... data) 
	{
		return null;
	}

	@Override
	public void refresh()
	{
		Set<ITransmitter<InventoryNetwork, ItemStack>> iterTransmitters = (Set<ITransmitter<InventoryNetwork, ItemStack>>)transmitters.clone();
		Iterator it = iterTransmitters.iterator();
		
		possibleAcceptors.clear();
		acceptorDirections.clear();

		while(it.hasNext())
		{
			ITransmitter<InventoryNetwork, ItemStack> conductor = (ITransmitter<InventoryNetwork, ItemStack>)it.next();

			if(conductor == null || ((TileEntity)conductor).isInvalid())
			{
				it.remove();
				transmitters.remove(conductor);
			}
			else {
				conductor.setTransmitterNetwork(this);
			}
		}
		
		for(ITransmitter<InventoryNetwork, ItemStack> transmitter : iterTransmitters)
		{
			IInventory[] inventories = TransporterUtils.getConnectedInventories((TileEntity)transmitter);
		
			for(IInventory inventory : inventories)
			{
				if(inventory != null && !(inventory instanceof ITransmitter))
				{
					possibleAcceptors.add(inventory);
					acceptorDirections.put(inventory, ForgeDirection.getOrientation(Arrays.asList(inventories).indexOf(inventory)));
				}
			}
		}
	}

	@Override
	public void merge(InventoryNetwork network)
	{
		if(network != null && network != this)
		{
			Set<InventoryNetwork> networks = new HashSet<InventoryNetwork>();
			networks.add(this);
			networks.add(network);
			InventoryNetwork newNetwork = new InventoryNetwork(networks);
			newNetwork.refresh();
		}
	}
		
	@Override
	public String toString()
	{
		return "[InventoryNetwork] " + transmitters.size() + " transmitters, " + possibleAcceptors.size() + " acceptors.";
	}

	@Override
	protected InventoryNetwork create(ITransmitter<InventoryNetwork, ItemStack>... varTransmitters) 
	{
		return new InventoryNetwork(varTransmitters);
	}

	@Override
	protected InventoryNetwork create(Collection<ITransmitter<InventoryNetwork, ItemStack>> collection) 
	{
		return new InventoryNetwork(collection);
	}

	@Override
	protected InventoryNetwork create(Set<InventoryNetwork> networks) 
	{
		return new InventoryNetwork(networks);
	}
	
	@Override
	public TransmissionType getTransmissionType()
	{
		return TransmissionType.ITEM;
	}

	@Override
	public String getFlow()
	{
		return "Undefined for Inventory networks";
	}

	@Override
	public String getNeeded()
	{
		return "Undefined for Inventory Networks";
	}
}
