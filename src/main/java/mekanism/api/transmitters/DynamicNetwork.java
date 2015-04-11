package mekanism.api.transmitters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map.Entry;
import java.util.Set;

import mekanism.api.Coord4D;
import mekanism.api.IClientTicker;
import mekanism.api.Range4D;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.Event;

public abstract class DynamicNetwork<A, N extends DynamicNetwork<A, N>> implements IClientTicker, INetworkDataHandler
{
	public LinkedHashSet<IGridTransmitter<A, N>> transmitters = new LinkedHashSet<>();
	public LinkedHashSet<IGridTransmitter<A, N>> transmittersToAdd = new LinkedHashSet<>();
	public LinkedHashSet<IGridTransmitter<A, N>> transmittersAdded = new LinkedHashSet<>();

	public HashMap<Coord4D, A> possibleAcceptors = new HashMap<Coord4D, A>();
	public HashMap<Coord4D, EnumSet<ForgeDirection>> acceptorDirections = new HashMap<Coord4D, EnumSet<ForgeDirection>>();
	public HashMap<IGridTransmitter<A, N>, EnumSet<ForgeDirection>> changedAcceptors = new HashMap<>();

	private Set<DelayQueue> updateQueue = new LinkedHashSet<DelayQueue>();

	protected Range4D packetRange = null;

	protected int capacity = 0;
	protected double meanCapacity = 0;

	protected boolean needsUpdate = false;
	protected int updateDelay = 0;

	protected boolean firstUpdate = true;
	protected World worldObj = null;

	public void addNewTransmitters(Collection<IGridTransmitter<A, N>> newTransmitters)
	{
		transmittersToAdd.addAll(newTransmitters);
	}

	public void commit()
	{
		if(!transmittersToAdd.isEmpty())
		{
			for(IGridTransmitter<A, N> transmitter : transmittersToAdd)
			{
				if(transmitter.isValid())
				{
					if(worldObj == null)
					{
						worldObj = transmitter.world();
					}

					for(ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
					{
						updateTransmitterOnSide(transmitter, side);
					}
					
					transmitter.setTransmitterNetwork((N)this);
					absorbBuffer(transmitter);
					transmitters.add(transmitter);
				}
			}
			
			updateCapacity();
			clampBuffer();
			queueClientUpdate(new ArrayList<>(transmittersToAdd));
			transmittersToAdd.clear();
		}

		if(!changedAcceptors.isEmpty())
		{
			for(Entry<IGridTransmitter<A, N>, EnumSet<ForgeDirection>> entry : changedAcceptors.entrySet())
			{
				IGridTransmitter<A, N> transmitter = entry.getKey();
				if(transmitter.isValid())
				{
					EnumSet<ForgeDirection> directionsChanged = entry.getValue();

					for(ForgeDirection side : directionsChanged)
					{
						updateTransmitterOnSide(transmitter, side);
					}
				}
			}
			
			changedAcceptors.clear();
		}
	}

	public void updateTransmitterOnSide(IGridTransmitter<A, N> transmitter, ForgeDirection side)
	{
		A acceptor = transmitter.getAcceptor(side);
		Coord4D acceptorCoord = transmitter.coord().getFromSide(side);
		EnumSet<ForgeDirection> directions = acceptorDirections.get(acceptorCoord);

		if(acceptor != null)
		{
			possibleAcceptors.put(acceptorCoord, acceptor);

			if(directions != null)
			{
				directions.add(side.getOpposite());
			}
			else {
				acceptorDirections.put(acceptorCoord, EnumSet.of(side.getOpposite()));
			}
		}
		else {
			if(directions != null)
			{
				directions.remove(side.getOpposite());

				if(directions.isEmpty())
				{
					possibleAcceptors.remove(acceptorCoord);
					acceptorDirections.remove(acceptorCoord);
				}
			}
			else
			{
				possibleAcceptors.remove(acceptorCoord);
				acceptorDirections.remove(acceptorCoord);
			}
		}

	}


	public abstract void absorbBuffer(IGridTransmitter<A, N> transmitter);

	public abstract void clampBuffer();

	public void invalidate()
	{
		for(IGridTransmitter<A, N> transmitter : transmitters)
		{
			invalidateTransmitter(transmitter);
		}
		
		transmitters.clear();
		deregister();
	}

	public void invalidateTransmitter(IGridTransmitter<A, N> transmitter)
	{
		if(!worldObj.isRemote && transmitter.isValid())
		{
			transmitter.takeShare();
			transmitter.setTransmitterNetwork(null);
			TransmitterNetworkRegistry.registerOrphanTransmitter(transmitter);
		}
	}

	public void acceptorChanged(IGridTransmitter<A, N> transmitter, ForgeDirection side)
	{
		EnumSet<ForgeDirection> directions = changedAcceptors.get(transmitter);
		
		if(directions != null)
		{
			directions.add(side);
		} 
		else {
			changedAcceptors.put(transmitter, EnumSet.of(side));
		}
		
		TransmitterNetworkRegistry.registerChangedNetwork(this);
	}

	public void adoptTransmittersAndAcceptorsFrom(N net)
	{
		for(IGridTransmitter<A, N> transmitter : net.transmitters)
		{
			transmitter.setTransmitterNetwork((N)this);
			transmitters.add(transmitter);
			transmittersAdded.add(transmitter);
		}
		
		possibleAcceptors.putAll(net.possibleAcceptors);
		
		for(Entry<Coord4D, EnumSet<ForgeDirection>> entry : net.acceptorDirections.entrySet())
		{
			Coord4D coord = entry.getKey();
			
			if(acceptorDirections.containsKey(coord))
			{
				acceptorDirections.get(coord).addAll(entry.getValue());
			}
			else {
				acceptorDirections.put(coord, entry.getValue());
			}
		}

	}

	public Range4D getPacketRange()
	{
		if(packetRange == null)
		{
			return genPacketRange();
		}
		
		return packetRange;
	}
	
	protected Range4D genPacketRange()
	{
		if(getSize() == 0)
		{
			deregister();
			return null;
		}

		IGridTransmitter<A, N> initTransmitter = transmitters.iterator().next();
		Coord4D initCoord = initTransmitter.coord();
		
		int minX = initCoord.xCoord;
		int minY = initCoord.yCoord;
		int minZ = initCoord.zCoord;
		int maxX = initCoord.xCoord;
		int maxY = initCoord.yCoord;
		int maxZ = initCoord.zCoord;
		
		for(IGridTransmitter transmitter : transmitters)
		{
			Coord4D coord = transmitter.coord();
			
			if(coord.xCoord < minX) minX = coord.xCoord;
			if(coord.yCoord < minY) minY = coord.yCoord;
			if(coord.zCoord < minZ) minZ = coord.zCoord;
			if(coord.xCoord > maxX) maxX = coord.xCoord;
			if(coord.yCoord > maxY) maxY = coord.yCoord;
			if(coord.zCoord > maxZ) maxZ = coord.zCoord;
		}
		
		return new Range4D(minX, minY, minZ, maxX, maxY, maxZ, initTransmitter.world().provider.dimensionId);
	}

	public void register()
	{
		if(FMLCommonHandler.instance().getEffectiveSide().isServer())
		{
			TransmitterNetworkRegistry.getInstance().registerNetwork(this);
		}
		else {
			MinecraftForge.EVENT_BUS.post(new ClientTickUpdate(this, (byte)1));
		}
	}

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

	public int getSize()
	{
		return transmitters.size();
	}

	public int getAcceptorSize()
	{
		return possibleAcceptors.size();
	}

	public synchronized void updateCapacity() 
	{
		updateMeanCapacity();
		capacity = (int)meanCapacity * transmitters.size();
	}

    /**
     * Override this if things can have variable capacity along the network.
     * @return An 'average' value of capacity. Calculate it how you will.
     */
	protected synchronized void updateMeanCapacity() 
	{
		if(transmitters.size() > 0) 
		{
			meanCapacity = transmitters.iterator().next().getCapacity();
		} 
		else {
			meanCapacity = 0;
		}
	}
	
    public int getCapacity()
    {
    	return capacity;
    }

	public World getWorld()
	{
		return worldObj;
	}

	public abstract Set<A> getAcceptors(Object data);

	public void tick()
	{
		onUpdate();
	}

	public void onUpdate()
	{
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
						transmittersAdded.addAll(transmitters);
						updateDelay = 1;
						i.remove();
					}
				}
			} catch(Exception e) {}

			if(updateDelay > 0)
			{
				updateDelay--;

				if(updateDelay == 0)
				{
					MinecraftForge.EVENT_BUS.post(new TransmittersAddedEvent(this, firstUpdate, (Collection)transmittersAdded));
					firstUpdate = false;
					transmittersAdded.clear();
					needsUpdate = true;
				}
			}
		}
	}

	@Override
	public boolean needsTicks()
	{
		return getSize() > 0;
	}

	@Override
	public void clientTick() {}

	public void queueClientUpdate(Collection<IGridTransmitter<A, N>> newTransmitters)
	{
		transmittersAdded.addAll(newTransmitters);
		updateDelay = 3;
	}

	public static class TransmittersAddedEvent extends Event
	{
		public DynamicNetwork<?, ?> network;
		public boolean newNetwork;
		public Collection<IGridTransmitter> newTransmitters;

		public TransmittersAddedEvent(DynamicNetwork net, boolean newNet, Collection<IGridTransmitter> added)
		{
			network = net;
			newNetwork = newNet;
			newTransmitters = added;
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

	public static class DelayQueue
	{
		public EntityPlayer player;
		public int delay;

		public DelayQueue(EntityPlayer p)
		{
			player = p;
			delay = 5;
		}

		@Override
		public int hashCode()
		{
			return player.hashCode();
		}

		@Override
		public boolean equals(Object o)
		{
			return o instanceof DelayQueue && ((DelayQueue)o).player.equals(this.player);
		}
	}
}
