package mekanism.api.transmitters;

import java.util.HashMap;
import java.util.HashSet;

import mekanism.api.Coord4D;

import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.common.gameevent.TickEvent.ServerTickEvent;
import cpw.mods.fml.relauncher.Side;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TransmitterNetworkRegistry
{
	private static TransmitterNetworkRegistry INSTANCE = new TransmitterNetworkRegistry();
	private static boolean loaderRegistered = false;

	private HashSet<DynamicNetwork> networks = new HashSet<>();
	private HashSet<DynamicNetwork> networksToChange = new HashSet<>();

	private HashSet<IGridTransmitter> invalidTransmitters = new HashSet<>();
	private HashMap<Coord4D, IGridTransmitter> orphanTransmitters = new HashMap<>();

	private Logger logger = LogManager.getLogger("MekanismTransmitters");

	public static void initiate()
	{
		if(!loaderRegistered)
		{
			loaderRegistered = true;

			FMLCommonHandler.instance().bus().register(INSTANCE);
		}
	}

	public static void reset()
	{
		getInstance().networks.clear();
		getInstance().networksToChange.clear();
		getInstance().invalidTransmitters.clear();
		getInstance().orphanTransmitters.clear();
	}

	public static void invalidateTransmitter(IGridTransmitter transmitter)
	{
		getInstance().invalidTransmitters.add(transmitter);
	}

	public static void registerOrphanTransmitter(IGridTransmitter transmitter)
	{
		getInstance().orphanTransmitters.put(transmitter.coord(), transmitter);
	}

	public static void registerChangedNetwork(DynamicNetwork network)
	{
		getInstance().networksToChange.add(network);
	}

	public static TransmitterNetworkRegistry getInstance()
	{
		return INSTANCE;
	}

	public void registerNetwork(DynamicNetwork network)
	{
		networks.add(network);
	}

	public void removeNetwork(DynamicNetwork network)
	{
		if(networks.contains(network))
		{
			networks.remove(network);
		}
	}

	@SubscribeEvent
	public void onTick(ServerTickEvent event)
	{
		if(event.phase == Phase.END && event.side == Side.SERVER)
		{
			tickEnd();
		}
	}

	public void tickEnd()
	{
		removeInvalidTransmitters();

		assignOrphans();

		commitChanges();

		for(DynamicNetwork net : networks)
		{
			net.tick();
		}
	}

	public void removeInvalidTransmitters()
	{
		if(!invalidTransmitters.isEmpty())
		{
			logger.debug("Dealing with " + invalidTransmitters.size() + " invalid Transmitters");
		}
		for(IGridTransmitter invalid : invalidTransmitters)
		{
			if(!invalid.isOrphan())
			{
				DynamicNetwork n = invalid.getTransmitterNetwork();
				if(n != null)
				{
					n.invalidate();
				}
			}
		}
		invalidTransmitters.clear();
	}

	public void assignOrphans()
	{
		if(!orphanTransmitters.isEmpty())
		{
			logger.debug("Dealing with " + orphanTransmitters.size() + " orphan Transmitters");
		}
		for(IGridTransmitter orphanTransmitter : orphanTransmitters.values())
		{
			DynamicNetwork network = getNetworkFromOrphan(orphanTransmitter);
			
			if(network != null)
			{
				networksToChange.add(network);
				network.register();
			}
		}
		
		orphanTransmitters.clear();
	}

	public <A, N extends DynamicNetwork<A, N>> DynamicNetwork<A, N> getNetworkFromOrphan(IGridTransmitter<A, N> startOrphan)
	{
		if(startOrphan.isValid() && startOrphan.isOrphan())
		{
			OrphanPathFinder<A, N> finder = new OrphanPathFinder<>(startOrphan);
			finder.start();
			N network;
			
			switch(finder.networksFound.size())
			{
				case 0:
					logger.debug("No networks found. Creating new network");
					network = startOrphan.createEmptyNetwork();
					break;
				case 1:
					logger.debug("Using single found network");
					network = finder.networksFound.iterator().next();
					break;
				default:
					logger.debug("Merging " + finder.networksFound.size() + " networks");
					network = startOrphan.mergeNetworks(finder.networksFound);
			}
			
			network.addNewTransmitters(finder.connectedTransmitters);
			
			return network;
		}
		
		return null;
	}

	public void commitChanges()
	{
		for(DynamicNetwork network : networksToChange)
		{
			network.commit();
		}
		
		networksToChange.clear();
	}

	@Override
	public String toString()
	{
		return "Network Registry:\n" + networks;
	}

	public String[] toStrings()
	{
		String[] strings = new String[networks.size()];
		int i = 0;

		for(DynamicNetwork network : networks)
		{
			strings[i++] = network.toString();
		}

		return strings;
	}

	public class OrphanPathFinder<A, N extends DynamicNetwork<A, N>>
	{
		public IGridTransmitter<A, N> startPoint;

		public HashSet<Coord4D> iterated = new HashSet<>();

		public HashSet<IGridTransmitter<A, N>> connectedTransmitters = new HashSet<>();
		public HashSet<N> networksFound = new HashSet<>();

		public OrphanPathFinder(IGridTransmitter<A, N> start)
		{
			startPoint = start;
		}

		public void start()
		{
			iterate(startPoint.coord(), ForgeDirection.UNKNOWN);
		}

		public void iterate(Coord4D from, ForgeDirection fromDirection)
		{
			if(iterated.contains(from))
			{
				return;
			}

			iterated.add(from);
			
			if(orphanTransmitters.containsKey(from))
			{
				IGridTransmitter<A, N> transmitter = orphanTransmitters.get(from);
				
				if(transmitter.isValid() && transmitter.isOrphan())
				{
					connectedTransmitters.add(transmitter);
					transmitter.setOrphan(false);
					
					for(ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS)
					{
						if(direction != fromDirection)
						{
							Coord4D directionCoord = transmitter.getAdjacentConnectableTransmitterCoord(direction);
							
							if(!(directionCoord == null || iterated.contains(directionCoord)))
							{
								iterate(directionCoord, direction.getOpposite());
							}
						}
					}
				}
			} 
			else {
				addNetworkToIterated(from);
			}
		}

		public void addNetworkToIterated(Coord4D from)
		{
			N net = startPoint.getExternalNetwork(from);
			if(net != null) networksFound.add(net);
		}
	}
}
