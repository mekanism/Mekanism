package mekanism.api.transmitters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import mekanism.api.IClientTicker;
import mekanism.api.Object3D;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.Event;
import cpw.mods.fml.common.FMLCommonHandler;

public abstract class DynamicNetwork<A, N extends DynamicNetwork<A, N>> implements ITransmitterNetwork<A, N>, IClientTicker
{
	public HashSet<ITransmitter<N>> transmitters = new HashSet<ITransmitter<N>>();
	
	public HashSet<A> possibleAcceptors = new HashSet<A>();
	public HashMap<A, ForgeDirection> acceptorDirections = new HashMap<A, ForgeDirection>();
	
	private List<DelayQueue> updateQueue = new ArrayList<DelayQueue>();
	
	protected int ticksSinceCreate = 0;
	
	protected boolean fixed = false;
	
	protected boolean needsUpdate = false;
	
	protected abstract ITransmitterNetwork<A, N> create(ITransmitter<N>... varTransmitters);
	
	protected abstract ITransmitterNetwork<A, N> create(Collection<ITransmitter<N>> collection);
	
	protected abstract ITransmitterNetwork<A, N> create(Set<N> networks);
	
	public void addAllTransmitters(Set<ITransmitter<N>> newTransmitters)
	{
		transmitters.addAll(newTransmitters);
	}
	
	@Override
	public void removeTransmitter(ITransmitter<N> transmitter)
	{
		transmitters.remove(transmitter);
		
		if(transmitters.size() == 0)
		{
			deregister();
		}
	}
	
	@Override
	public void register()
	{
		try {
			ITransmitter<N> aTransmitter = transmitters.iterator().next();
			
			if(aTransmitter instanceof TileEntity)
			{
				if(!((TileEntity)aTransmitter).worldObj.isRemote)
				{
					TransmitterNetworkRegistry.getInstance().registerNetwork(this);			
				}
				else {
					MinecraftForge.EVENT_BUS.post(new ClientTickUpdate(this, (byte)1));
				}
			}
		} catch(NoSuchElementException e) {}
	}
	
	@Override
	public void deregister()
	{
		transmitters.clear();
		
		if(FMLCommonHandler.instance().getEffectiveSide().isServer())
		{
			TransmitterNetworkRegistry.getInstance().removeNetwork(this);
		}
		else {
			MinecraftForge.EVENT_BUS.post(new ClientTickUpdate(this, (byte)0));
		}
	}
	
	@Override
	public int getSize()
	{
		return transmitters.size();
	}
	
	@Override
	public int getAcceptorSize()
	{
		return possibleAcceptors.size();
	}

    public int getCapacity()
    {
        return (int)getMeanCapacity() * transmitters.size();
    }

    /**
     * Override this if things can have variable capacity along the network.
     * @return An 'average' value of capacity. Calculate it how you will.
     */
    public double getMeanCapacity()
    {
		return transmitters.size() > 0 ? transmitters.iterator().next().getCapacity() : 0;
    }
	
	@Override
	public void tick()
	{
		if(!fixed)
		{
			++ticksSinceCreate;
			
			if(ticksSinceCreate > 1200)
			{
				ticksSinceCreate = 0;
				fixMessedUpNetwork(transmitters.iterator().next());
			}
		}
		
		if(FMLCommonHandler.instance().getEffectiveSide().isServer())
		{
			Iterator<DelayQueue> i = updateQueue.iterator();
			
			try {
				while(i.hasNext())
				{
					DelayQueue q = i.next();
					
					if(q.delay > 0)
					{
						q.delay--;
					}
					else {
						needsUpdate = true;
						i.remove();
					}
				}
			} catch(Exception e) {}
		}
	}
	
	@Override
	public synchronized void fixMessedUpNetwork(ITransmitter<N> transmitter)
	{
		if(transmitter instanceof TileEntity)
		{
			NetworkFinder finder = new NetworkFinder(((TileEntity)transmitter).getWorldObj(), getTransmissionType(), Object3D.get((TileEntity)transmitter));
			List<Object3D> partNetwork = finder.exploreNetwork();
			Set<ITransmitter<N>> newTransporters = new HashSet<ITransmitter<N>>();
			
			for(Object3D node : partNetwork)
			{
				TileEntity nodeTile = node.getTileEntity(((TileEntity)transmitter).worldObj);

				if(TransmissionType.checkTransmissionType(nodeTile, getTransmissionType(), (TileEntity)transmitter))
				{
					((ITransmitter<N>)nodeTile).removeFromTransmitterNetwork();
					newTransporters.add((ITransmitter<N>)nodeTile);
				}
			}
			
			ITransmitterNetwork<A, N> newNetwork = create(newTransporters);
			newNetwork.refresh();
			newNetwork.setFixed(true);
			deregister();
		}
	}
	
	@Override
	public synchronized void split(ITransmitter<N> splitPoint)
	{
		if(splitPoint instanceof TileEntity)
		{
			removeTransmitter(splitPoint);
			
			TileEntity[] connectedBlocks = new TileEntity[6];
			boolean[] dealtWith = {false, false, false, false, false, false};
			List<NetworkFinder> newNetworks = new ArrayList<NetworkFinder>();
			
			for(ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
			{
				TileEntity sideTile = Object3D.get((TileEntity)splitPoint).getFromSide(side).getTileEntity(((TileEntity)splitPoint).worldObj);
				
				if(sideTile != null)
				{
					connectedBlocks[side.ordinal()] = sideTile;
				}
			}

			for(int count = 0; count < connectedBlocks.length; count++)
			{
				TileEntity connectedBlockA = connectedBlocks[count];

				if(TransmissionType.checkTransmissionType(connectedBlockA, getTransmissionType()) && !dealtWith[count])
				{
					NetworkFinder finder = new NetworkFinder(((TileEntity)splitPoint).worldObj, getTransmissionType(), Object3D.get(connectedBlockA), Object3D.get((TileEntity)splitPoint));
					List<Object3D> partNetwork = finder.exploreNetwork();
					
					for(int check = count; check < connectedBlocks.length; check++)
					{
						if(check == count)
						{
							continue;
						}
						
						TileEntity connectedBlockB = connectedBlocks[check];
						
						if(TransmissionType.checkTransmissionType(connectedBlockB, getTransmissionType()) && !dealtWith[check])
						{
							if(partNetwork.contains(Object3D.get(connectedBlockB)))
							{
								dealtWith[check] = true;
							}
						}
					}
					
					newNetworks.add(finder);
				}
			}
			
			if(newNetworks.size() > 0)
			{
				Object[] metaArray = getMetaArray(newNetworks);
				
				for(NetworkFinder finder : newNetworks)
				{
					Set<ITransmitter<N>> newNetCables = new HashSet<ITransmitter<N>>();
					
					for(Object3D node : finder.iterated)
					{
						TileEntity nodeTile = node.getTileEntity(((TileEntity)splitPoint).worldObj);
	
						if(TransmissionType.checkTransmissionType(nodeTile, getTransmissionType()))
						{
							if(nodeTile != splitPoint)
							{
								newNetCables.add((ITransmitter<N>)nodeTile);
							}
						}
					}
					
					ITransmitterNetwork<A, N> newNetwork = create(newNetCables);
					newNetwork.onNewFromSplit(newNetworks.indexOf(finder), metaArray);
					newNetwork.refresh();
				}
			}
			
			deregister();
		}
	}
	
	@Override
	public Object[] getMetaArray(List<NetworkFinder> networks)
	{
		return null;
	}
	
	@Override
	public void onNewFromSplit(int netIndex, Object[] metaArray) {}
	
	@Override
	public void setFixed(boolean value)
	{
		fixed = value;
	}
	
	@Override
	public boolean needsTicks()
	{
		return getSize() > 0;
	}
	
	@Override
	public void clientTick() 
	{
		ticksSinceCreate++;
		
		if(ticksSinceCreate == 5 && getSize() > 0)
		{
			TileEntity tile = (TileEntity)transmitters.iterator().next();
			MinecraftForge.EVENT_BUS.post(new NetworkClientRequest(tile));
		}
	}
	
	public static class ClientTickUpdate extends Event
	{
		public DynamicNetwork network;
		public byte operation; /*0 remove, 1 add*/
		
		public ClientTickUpdate(DynamicNetwork net, byte b)
		{
			network = net;
			operation = b;
		}
	}
	
	public static class NetworkClientRequest extends Event
	{
		public TileEntity tileEntity;
		
		public NetworkClientRequest(TileEntity tile)
		{
			tileEntity = tile;
		}
	}
	
	public void addUpdate(EntityPlayer player)
	{
		updateQueue.add(new DelayQueue(player));
	}
	
	public static class NetworkFinder
	{
		public TransmissionType transmissionType;
		
		public World worldObj;
		public Object3D start;
		
		public List<Object3D> iterated = new ArrayList<Object3D>();
		public List<Object3D> toIgnore = new ArrayList<Object3D>();
		
		public NetworkFinder(World world, TransmissionType type, Object3D location, Object3D... ignore)
		{
			worldObj = world;
			start = location;
			
			transmissionType = type;
			
			if(ignore != null)
			{
			    for(int i = 0; i < ignore.length; i++)
			    {
			        toIgnore.add(ignore[i]);
			    }
			}
		}

		public void loopAll(Object3D location)
		{
			if(TransmissionType.checkTransmissionType(location.getTileEntity(worldObj), transmissionType))
			{
				iterated.add(location);
			}
			else {
			    toIgnore.add(location);
			}
			
			for(ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS)
			{
				Object3D obj = location.getFromSide(direction);
				
				if(!iterated.contains(obj) && !toIgnore.contains(obj))
				{
					TileEntity tileEntity = obj.getTileEntity(worldObj);
					
					if(TransmissionType.checkTransmissionType(tileEntity, transmissionType, location.getTileEntity(worldObj)))
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
	
	public static class DelayQueue
	{
		public EntityPlayer player;
		public int delay;
		
		public DelayQueue(EntityPlayer p)
		{
			player = p;
			delay = 5;
		}
	}
}
