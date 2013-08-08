package mekanism.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.Event;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.world.ChunkEvent;
import cpw.mods.fml.common.FMLCommonHandler;

public class GasNetwork implements ITransmitterNetwork
{
	public HashSet<IPressurizedTube> tubes = new HashSet<IPressurizedTube>();
	
	public Set<IGasAcceptor> possibleAcceptors = new HashSet<IGasAcceptor>();
	public Map<IGasAcceptor, ForgeDirection> acceptorDirections = new HashMap<IGasAcceptor, ForgeDirection>();
	
	private int ticksSinceCreate = 0;
	private boolean fixed = false;
	
	public GasNetwork(IPressurizedTube... varPipes)
	{
		tubes.addAll(Arrays.asList(varPipes));
		register();
	}
	
	public GasNetwork(Set<GasNetwork> networks)
	{
		for(GasNetwork net : networks)
		{
			if(net != null)
			{
				addAllTubes(net.tubes);
				net.deregister();
			}
		}
		
		refresh();
		register();
	}
	
	public int emit(int gasToSend, EnumGas transferType, TileEntity emitter)
	{
		List availableAcceptors = Arrays.asList(getGasAcceptors(transferType).toArray());
		
		Collections.shuffle(availableAcceptors);
		
		int prevSending = gasToSend;
		
		if(!availableAcceptors.isEmpty())
		{
			int divider = availableAcceptors.size();
			int remaining = gasToSend % divider;
			int sending = (gasToSend-remaining)/divider;
			
			for(Object obj : availableAcceptors)
			{
				if(obj instanceof IGasAcceptor && obj != emitter)
				{
					IGasAcceptor acceptor = (IGasAcceptor)obj;
					
					int currentSending = sending;
					
					if(remaining > 0)
					{
						currentSending++;
						remaining--;
					}
					
					gasToSend -= (currentSending - acceptor.transferGasToAcceptor(currentSending, transferType));
				}
			}
		}
		
		if(prevSending > gasToSend && FMLCommonHandler.instance().getEffectiveSide().isServer())
		{
			MinecraftForge.EVENT_BUS.post(new GasTransferEvent(this, transferType));
		}
		
		return gasToSend;
	}
	
	public Set<IGasAcceptor> getGasAcceptors(EnumGas transferType)
	{
		Set<IGasAcceptor> toReturn = new HashSet<IGasAcceptor>();
		
		for(IGasAcceptor acceptor : possibleAcceptors)
		{
			if(acceptor.canReceiveGas(acceptorDirections.get(acceptor).getOpposite(), transferType))
			{
				if(!(acceptor instanceof IGasStorage) || (acceptor instanceof IGasStorage && (((IGasStorage)acceptor).getMaxGas(transferType) - ((IGasStorage)acceptor).getGas(transferType)) > 0))
				{
					toReturn.add(acceptor);
				}
			}
		}
		
		return toReturn;
	}

	public void refresh()
	{
		Set<IPressurizedTube> iterTubes = (Set<IPressurizedTube>) tubes.clone();
		Iterator<IPressurizedTube> it = iterTubes.iterator();
		
		possibleAcceptors.clear();
		acceptorDirections.clear();

		while(it.hasNext())
		{
			IPressurizedTube conductor = (IPressurizedTube)it.next();

			if(conductor == null || ((TileEntity)conductor).isInvalid())
			{
				it.remove();
				tubes.remove(conductor);
			}
			else {
				conductor.setNetwork(this);
			}
		}
		
		for(IPressurizedTube pipe : tubes)
		{
			IGasAcceptor[] acceptors = GasTransmission.getConnectedAcceptors((TileEntity)pipe);
		
			for(IGasAcceptor acceptor : acceptors)
			{
				if(acceptor != null && !(acceptor instanceof IPressurizedTube))
				{
					possibleAcceptors.add(acceptor);
					acceptorDirections.put(acceptor, ForgeDirection.getOrientation(Arrays.asList(acceptors).indexOf(acceptor)));
				}
			}
		}
	}

	public void merge(GasNetwork network)
	{
		if(network != null && network != this)
		{
			Set<GasNetwork> networks = new HashSet();
			networks.add(this);
			networks.add(network);
			GasNetwork newNetwork = new GasNetwork(networks);
			newNetwork.refresh();
		}
	}

	public void addAllTubes(Set<IPressurizedTube> newTubes)
	{
		tubes.addAll(newTubes);
	}

	public void split(IPressurizedTube splitPoint)
	{
		if(splitPoint instanceof TileEntity)
		{
			removeTube(splitPoint);
			
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

				if(connectedBlockA instanceof IPressurizedTube && !dealtWith[countOne])
				{
					NetworkFinder finder = new NetworkFinder(((TileEntity)splitPoint).worldObj, Object3D.get(connectedBlockA), Object3D.get((TileEntity)splitPoint));
					List<Object3D> partNetwork = finder.exploreNetwork();
					
					for(int countTwo = countOne + 1; countTwo < connectedBlocks.length; countTwo++)
					{
						TileEntity connectedBlockB = connectedBlocks[countTwo];

						if(connectedBlockB instanceof IPressurizedTube && !dealtWith[countTwo])
						{
							if(partNetwork.contains(Object3D.get(connectedBlockB)))
							{
								dealtWith[countTwo] = true;
							}
						}
					}
					
					GasNetwork newNetwork = new GasNetwork();
					
					for(Object3D node : finder.iterated)
					{
						TileEntity nodeTile = node.getTileEntity(((TileEntity)splitPoint).worldObj);

						if(nodeTile instanceof IPressurizedTube)
						{
							if(nodeTile != splitPoint)
							{
								newNetwork.tubes.add((IPressurizedTube)nodeTile);
							}
						}
					}
					
					newNetwork.refresh();
				}
			}
			
			deregister();
		}
	}
	
	public void fixMessedUpNetwork(IPressurizedTube tube)
	{
		if(tube instanceof TileEntity)
		{
			NetworkFinder finder = new NetworkFinder(((TileEntity)tube).getWorldObj(), Object3D.get((TileEntity)tube), null);
			List<Object3D> partNetwork = finder.exploreNetwork();
			Set<IPressurizedTube> newTubes = new HashSet<IPressurizedTube>();
			
			for(Object3D node : partNetwork)
			{
				TileEntity nodeTile = node.getTileEntity(((TileEntity)tube).worldObj);

				if(nodeTile instanceof IPressurizedTube)
				{
					((IPressurizedTube) nodeTile).removeFromNetwork();
					newTubes.add((IPressurizedTube)nodeTile);
				}
			}
			
			GasNetwork newNetwork = new GasNetwork(newTubes.toArray(new IPressurizedTube[0]));
			newNetwork.refresh();
			newNetwork.fixed = true;
			deregister();
		}
	}
	
	public void removeTube(IPressurizedTube tube)
	{
		tubes.remove(tube);
		if(tubes.size() == 0)
		{
			deregister();
		}
	}
	
	public void register()
	{
		try {
			IPressurizedTube aTube = tubes.iterator().next();
			
			if(aTube instanceof TileEntity && !((TileEntity)aTube).worldObj.isRemote)
			{
				TransmitterNetworkRegistry.getInstance().registerNetwork(this);			
			}
		} catch(NoSuchElementException e) {}
	}
	
	public void deregister()
	{
		tubes.clear();
		TransmitterNetworkRegistry.getInstance().removeNetwork(this);
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
			if(location.getTileEntity(worldObj) instanceof IPressurizedTube)
			{
				iterated.add(location);
			}
			
			for(ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS)
			{
				Object3D obj = location.getFromSide(direction);
				
				if(!iterated.contains(obj) && !toIgnore.contains(obj))
				{
					TileEntity tileEntity = obj.getTileEntity(worldObj);
					
					if(tileEntity instanceof IPressurizedTube)
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
	
	public static class GasTransferEvent extends Event
	{
		public final GasNetwork gasNetwork;
		
		public final EnumGas transferType;
		
		public GasTransferEvent(GasNetwork network, EnumGas type)
		{
			gasNetwork = network;
			transferType = type;
		}
	}
	
	public static class NetworkLoader
	{
		@ForgeSubscribe
		public void onChunkLoad(ChunkEvent.Load event)
		{
			if(event.getChunk() != null)
			{
				for(Object obj : event.getChunk().chunkTileEntityMap.values())
				{
					if(obj instanceof TileEntity)
					{
						TileEntity tileEntity = (TileEntity)obj;
						
						if(tileEntity instanceof IPressurizedTube)
						{
							((IPressurizedTube)tileEntity).refreshNetwork();
						}
					}
				}
			}
		}
	}
	
	@Override
	public String toString()
	{
		return "[GasNetwork] " + tubes.size() + " pipes, " + possibleAcceptors.size() + " acceptors.";
	}

	public void tick()
	{		
		//Fix weird behaviour periodically.
		if(!fixed)
		{
			++ticksSinceCreate;
			if(ticksSinceCreate > 1200)
			{
				ticksSinceCreate = 0;
				fixMessedUpNetwork(tubes.iterator().next());
			}
		}
	}

	@Override
	public int getSize()
	{
		return tubes.size();
	}
}
