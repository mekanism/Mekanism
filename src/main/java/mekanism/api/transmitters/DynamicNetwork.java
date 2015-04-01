package mekanism.api.transmitters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import mekanism.api.Coord4D;
import mekanism.api.IClientTicker;
import mekanism.api.Range4D;
import mekanism.api.energy.EnergyAcceptorWrapper;

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

	public HashMap<Coord4D, A> possibleAcceptors = new HashMap<Coord4D, A>();
	public HashMap<Coord4D, EnumSet<ForgeDirection>> acceptorDirections = new HashMap<Coord4D, EnumSet<ForgeDirection>>();

	private List<DelayQueue> updateQueue = new ArrayList<DelayQueue>();

	protected Range4D packetRange = null;

	protected int ticksSinceCreate = 0;

	protected int capacity = 0;
	protected double meanCapacity = 0;

	protected boolean needsUpdate = false;

	protected World worldObj = null;

	public void addNewTransmitters(Collection<IGridTransmitter<A, N>> newTransmitters)
	{
		transmittersToAdd.addAll(newTransmitters);
	}

	public void commit()
	{
		for(IGridTransmitter<A, N> transmitter : transmittersToAdd)
		{
			if(transmitter.isValid())
			{
				if(worldObj == null) worldObj = transmitter.world();
				Coord4D coord = transmitter.coord();
				for(ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
				{
					A acceptor = transmitter.getAcceptor(side);
					if(acceptor != null)
					{
						possibleAcceptors.put(coord, acceptor);
						EnumSet<ForgeDirection> directions = acceptorDirections.get(coord.getFromSide(side));
						if(directions != null)
						{
							directions.add(side.getOpposite());
						} else
						{
							acceptorDirections.put(coord, EnumSet.of(side.getOpposite()));
						}
					}
				}
				transmitter.setOrphan(false);
				transmitter.setTransmitterNetwork((N)this);
				absorbBuffer(transmitter);
				transmitters.add(transmitter);
			}
		}
		updateCapacity();
		clampBuffer();
		transmittersToAdd.clear();
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
			transmitter.setOrphan(true);
			TransmitterNetworkRegistry.registerOrphanTransmitter(transmitter);
		}
	}

	public void adoptTransmittersAndAcceptorsFrom(N net)
	{
		for(IGridTransmitter<A, N> transmitter : net.transmitters)
		{
			transmitter.setTransmitterNetwork((N)this);
			transmitters.add(transmitter);
		}
		possibleAcceptors.putAll(net.possibleAcceptors);
		for(Entry<Coord4D, EnumSet<ForgeDirection>> entry : net.acceptorDirections.entrySet())
		{
			Coord4D coord = entry.getKey();
			if(acceptorDirections.containsKey(coord))
			{
				acceptorDirections.get(coord).addAll(entry.getValue());
			}
			else
			{
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
						needsUpdate = true;
						i.remove();
					}
				}
			} catch(Exception e) {}
		}
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
