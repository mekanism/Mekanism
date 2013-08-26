package mekanism.api.transmitters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import mekanism.api.Object3D;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;

public abstract class DynamicNetwork<A, N> implements ITransmitterNetwork<A, N>
{
	public HashSet<ITransmitter<N>> transmitters = new HashSet<ITransmitter<N>>();
	
	public Set<A> possibleAcceptors = new HashSet<A>();
	public Map<A, ForgeDirection> acceptorDirections = new HashMap<A, ForgeDirection>();
	
	protected int ticksSinceCreate = 0;
	protected int ticksSinceSecond = 0;
	
	protected boolean fixed = false;
	
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
			
			if(aTransmitter instanceof TileEntity && !((TileEntity)aTransmitter).worldObj.isRemote)
			{
				TransmitterNetworkRegistry.getInstance().registerNetwork(this);			
			}
		} catch(NoSuchElementException e) {}
	}
	
	@Override
	public void deregister()
	{
		transmitters.clear();
		TransmitterNetworkRegistry.getInstance().removeNetwork(this);
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
	}
	
	@Override
	public void fixMessedUpNetwork(ITransmitter<N> transmitter)
	{
		if(transmitter instanceof TileEntity)
		{
			NetworkFinder finder = new NetworkFinder(((TileEntity)transmitter).getWorldObj(), getTransmissionType(), Object3D.get((TileEntity)transmitter));
			List<Object3D> partNetwork = finder.exploreNetwork();
			Set<ITransmitter<N>> newTransporters = new HashSet<ITransmitter<N>>();
			
			for(Object3D node : partNetwork)
			{
				TileEntity nodeTile = node.getTileEntity(((TileEntity)transmitter).worldObj);

				if(TransmissionType.checkTransmissionType(nodeTile, getTransmissionType(), (TileEntity) transmitter))
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
	public void split(ITransmitter<N> splitPoint)
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

				if(TransmissionType.checkTransmissionType(connectedBlockA, getTransmissionType()) && !dealtWith[countOne])
				{
					NetworkFinder finder = new NetworkFinder(((TileEntity)splitPoint).worldObj, getTransmissionType(), Object3D.get(connectedBlockA), Object3D.get((TileEntity)splitPoint));
					List<Object3D> partNetwork = finder.exploreNetwork();
					
					for(int countTwo = countOne + 1; countTwo < connectedBlocks.length; countTwo++)
					{
						TileEntity connectedBlockB = connectedBlocks[countTwo];
						
						if(TransmissionType.checkTransmissionType(connectedBlockB, getTransmissionType()) && !dealtWith[countTwo])
						{
							if(partNetwork.contains(Object3D.get(connectedBlockB)))
							{
								dealtWith[countTwo] = true;
							}
						}
					}
					
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
					newNetwork.refresh();
				}
			}
			
			deregister();
		}
	}
	
	@Override
	public void setFixed(boolean value)
	{
		fixed = value;
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
			    for (int i = 0; i < ignore.length; i++)
			    {
			        this.toIgnore.add(ignore[i]);
			    }
			}
		}

		public void loopAll(Object3D location)
		{
			if(TransmissionType.checkTransmissionType(location.getTileEntity(worldObj), transmissionType))
			{
				iterated.add(location);
			}
			else
			{
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
}
