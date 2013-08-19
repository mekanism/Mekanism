package mekanism.api;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;

public abstract class DynamicNetwork<A, N> implements ITransmitterNetwork<A, N>
{
	public HashSet<ITransmitter<N>> transmitters = new HashSet<ITransmitter<N>>();
	
	public Set<A> possibleAcceptors = new HashSet<A>();
	public Map<A, ForgeDirection> acceptorDirections = new HashMap<A, ForgeDirection>();
	
	protected int ticksSinceCreate = 0;
	protected int ticksSinceSecond = 0;
	
	protected boolean fixed = false;
	
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
}
