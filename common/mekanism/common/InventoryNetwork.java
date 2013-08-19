package mekanism.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import mekanism.api.DynamicNetwork;
import mekanism.api.ITransmitter;
import mekanism.api.Object3D;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;

public class InventoryNetwork extends DynamicNetwork<IInventory, InventoryNetwork>
{
	public InventoryNetwork(ITransmitter<InventoryNetwork>... varTransporters)
	{
		transmitters.addAll(Arrays.asList(varTransporters));
		register();
	}
	
	public InventoryNetwork(Collection<ITransmitter<InventoryNetwork>> collection)
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
		Set<ITransmitter<InventoryNetwork>> iterTransmitters = (Set<ITransmitter<InventoryNetwork>>)transmitters.clone();
		Iterator it = iterTransmitters.iterator();
		
		possibleAcceptors.clear();
		acceptorDirections.clear();

		while(it.hasNext())
		{
			ITransmitter<InventoryNetwork> conductor = (ITransmitter<InventoryNetwork>)it.next();

			if(conductor == null || ((TileEntity)conductor).isInvalid())
			{
				it.remove();
				transmitters.remove(conductor);
			}
			else {
				conductor.setNetwork(this);
			}
		}
		
		for(ITransmitter<InventoryNetwork> transmitter : iterTransmitters)
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
	public void split(ITransmitter<InventoryNetwork> splitPoint)
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

				if(MekanismUtils.checkNetwork(connectedBlockA, InventoryNetwork.class) && !dealtWith[countOne])
				{
					NetworkFinder finder = new NetworkFinder(((TileEntity)splitPoint).worldObj, Object3D.get(connectedBlockA), Object3D.get((TileEntity)splitPoint));
					List<Object3D> partNetwork = finder.exploreNetwork();
					
					for(int countTwo = countOne + 1; countTwo < connectedBlocks.length; countTwo++)
					{
						TileEntity connectedBlockB = connectedBlocks[countTwo];
						
						if(MekanismUtils.checkNetwork(connectedBlockB, InventoryNetwork.class) && !dealtWith[countTwo])
						{
							if(partNetwork.contains(Object3D.get(connectedBlockB)))
							{
								dealtWith[countTwo] = true;
							}
						}
					}
					
					Set<ITransmitter<InventoryNetwork>> newNetTransporters = new HashSet<ITransmitter<InventoryNetwork>>();
					
					for(Object3D node : finder.iterated)
					{
						TileEntity nodeTile = node.getTileEntity(((TileEntity)splitPoint).worldObj);

						if(MekanismUtils.checkNetwork(nodeTile, InventoryNetwork.class))
						{
							if(nodeTile != splitPoint)
							{
								newNetTransporters.add((ITransmitter<InventoryNetwork>)nodeTile);
							}
						}
					}
					
					InventoryNetwork newNetwork = new InventoryNetwork(newNetTransporters);					
					newNetwork.refresh();
				}
			}
			
			deregister();
		}
	}
	
	@Override
	public void fixMessedUpNetwork(ITransmitter<InventoryNetwork> transmitter)
	{
		if(transmitter instanceof TileEntity)
		{
			NetworkFinder finder = new NetworkFinder(((TileEntity)transmitter).getWorldObj(), Object3D.get((TileEntity)transmitter), null);
			List<Object3D> partNetwork = finder.exploreNetwork();
			Set<ITransmitter<InventoryNetwork>> newTransporters = new HashSet<ITransmitter<InventoryNetwork>>();
			
			for(Object3D node : partNetwork)
			{
				TileEntity nodeTile = node.getTileEntity(((TileEntity)transmitter).worldObj);

				if(MekanismUtils.checkNetwork(nodeTile, InventoryNetwork.class))
				{
					((ITransmitter<InventoryNetwork>)nodeTile).removeFromNetwork();
					newTransporters.add((ITransmitter<InventoryNetwork>)nodeTile);
				}
			}
			
			InventoryNetwork newNetwork = new InventoryNetwork(newTransporters);
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
			if(MekanismUtils.checkNetwork(location.getTileEntity(worldObj), InventoryNetwork.class))
			{
				iterated.add(location);
			}
			
			for(ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS)
			{
				Object3D obj = location.getFromSide(direction);
				
				if(!iterated.contains(obj) && !toIgnore.contains(obj))
				{
					TileEntity tileEntity = obj.getTileEntity(worldObj);
					
					if(MekanismUtils.checkNetwork(tileEntity, InventoryNetwork.class))
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
