package mekanism.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import mekanism.api.DynamicNetwork;
import mekanism.api.ITransmitterNetwork;
import mekanism.api.Object3D;
import mekanism.api.TransmitterNetworkRegistry;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.world.ChunkEvent;

public class InventoryNetwork extends DynamicNetwork<ILogisticalTransporter, IInventory, InventoryNetwork>
{
	public InventoryNetwork(ILogisticalTransporter... varTransporters)
	{
		transmitters.addAll(Arrays.asList(varTransporters));
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
		Set<ILogisticalTransporter> iterPipes = (Set<ILogisticalTransporter>)transmitters.clone();
		Iterator it = iterPipes.iterator();
		
		possibleAcceptors.clear();
		acceptorDirections.clear();

		while(it.hasNext())
		{
			ILogisticalTransporter conductor = (ILogisticalTransporter)it.next();

			if(conductor == null || ((TileEntity)conductor).isInvalid())
			{
				it.remove();
				transmitters.remove(conductor);
			}
			else {
				conductor.setNetwork(this);
			}
		}
		
		for(ILogisticalTransporter pipe : iterPipes)
		{
			IInventory[] inventories = TransporterUtils.getConnectedInventories((TileEntity)pipe);
		
			for(IInventory inventory : inventories)
			{
				if(inventory != null && !(inventory instanceof ILogisticalTransporter))
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
	public void split(ILogisticalTransporter splitPoint)
	{
		if(splitPoint instanceof TileEntity)
		{
			removeTransmitter(splitPoint);
			
			TileEntity[] connectedBlocks = new TileEntity[6];
			boolean[] dealtWith = {false, false, false, false, false, false};
			
			for(ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS)
			{
				TileEntity sideTile = Object3D.get((TileEntity)splitPoint).getFromSide(direction).getTileEntity(((TileEntity)splitPoint).worldObj);
				
				if(sideTile != null)
				{
					connectedBlocks[Arrays.asList(ForgeDirection.values()).indexOf(direction)] = sideTile;
				}
			}

			for(int countOne = 0; countOne < connectedBlocks.length; countOne++)
			{
				TileEntity connectedBlockA = connectedBlocks[countOne];

				if(connectedBlockA instanceof ILogisticalTransporter && !dealtWith[countOne])
				{
					NetworkFinder finder = new NetworkFinder(((TileEntity)splitPoint).worldObj, Object3D.get(connectedBlockA), Object3D.get((TileEntity)splitPoint));
					List<Object3D> partNetwork = finder.exploreNetwork();
					
					for(int countTwo = countOne + 1; countTwo < connectedBlocks.length; countTwo++)
					{
						TileEntity connectedBlockB = connectedBlocks[countTwo];
						
						if(connectedBlockB instanceof ILogisticalTransporter && !dealtWith[countTwo])
						{
							if(partNetwork.contains(Object3D.get(connectedBlockB)))
							{
								dealtWith[countTwo] = true;
							}
						}
					}
					
					Set<ILogisticalTransporter> newNetTransporters= new HashSet<ILogisticalTransporter>();
					
					for(Object3D node : finder.iterated)
					{
						TileEntity nodeTile = node.getTileEntity(((TileEntity)splitPoint).worldObj);

						if(nodeTile instanceof ILogisticalTransporter)
						{
							if(nodeTile != splitPoint)
							{
								newNetTransporters.add((ILogisticalTransporter)nodeTile);
							}
						}
					}
					
					InventoryNetwork newNetwork = new InventoryNetwork(newNetTransporters.toArray(new ILogisticalTransporter[0]));					
					newNetwork.refresh();
				}
			}
			
			deregister();
		}
	}
	
	@Override
	public void fixMessedUpNetwork(ILogisticalTransporter pipe)
	{
		if(pipe instanceof TileEntity)
		{
			NetworkFinder finder = new NetworkFinder(((TileEntity)pipe).getWorldObj(), Object3D.get((TileEntity)pipe), null);
			List<Object3D> partNetwork = finder.exploreNetwork();
			Set<ILogisticalTransporter> newTransporters = new HashSet<ILogisticalTransporter>();
			
			for(Object3D node : partNetwork)
			{
				TileEntity nodeTile = node.getTileEntity(((TileEntity)pipe).worldObj);

				if(nodeTile instanceof ILogisticalTransporter)
				{
					((ILogisticalTransporter)nodeTile).removeFromNetwork();
					newTransporters.add((ILogisticalTransporter)nodeTile);
				}
			}
			
			InventoryNetwork newNetwork = new InventoryNetwork(newTransporters.toArray(new ILogisticalTransporter[0]));
			newNetwork.refresh();
			newNetwork.fixed = true;
			deregister();
		}
	}
	
	public static class NetworkFinder
	{
		public World worldObj;
		public Object3D start;
		
		public List<Object3D> iterated = new ArrayList<Object3D>();
		public List<Object3D> toIgnore = new ArrayList<Object3D>();
		
		public NetworkFinder(World world, Object3D location, Object3D... ignore)
		{
			worldObj = world;
			start = location;
			
			if(ignore != null)
			{
				toIgnore = Arrays.asList(ignore);
			}
		}

		public void loopAll(Object3D location)
		{
			if(location.getTileEntity(worldObj) instanceof ILogisticalTransporter)
			{
				iterated.add(location);
			}
			
			for(ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS)
			{
				Object3D obj = location.getFromSide(direction);
				
				if(!iterated.contains(obj) && !toIgnore.contains(obj))
				{
					TileEntity tileEntity = obj.getTileEntity(worldObj);
					
					if(tileEntity instanceof ILogisticalTransporter)
					{
						loopAll(obj);
					}
				}
			}
		}

		public List<Object3D> exploreNetwork()
		{
			loopAll(start);
			
			return iterated;
		}
	}
		
	@Override
	public String toString()
	{
		return "[InventoryNetwork] " + transmitters.size() + " transmitters, " + possibleAcceptors.size() + " acceptors.";
	}
}
