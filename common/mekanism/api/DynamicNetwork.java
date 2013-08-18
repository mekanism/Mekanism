package mekanism.api;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;

import mekanism.common.EnergyNetwork;
import mekanism.common.IMechanicalPipe;
import mekanism.common.IUniversalCable;

public abstract class DynamicNetwork<T, A, N> implements ITransmitterNetwork<T, A, N>
{
	public HashSet<T> transmitters = new HashSet<T>();
	
	public Set<A> possibleAcceptors = new HashSet<A>();
	public Map<A, ForgeDirection> acceptorDirections = new HashMap<A, ForgeDirection>();
	
	protected int ticksSinceCreate = 0;
	protected int ticksSinceSecond = 0;
	
	protected boolean fixed = false;
	
	public void addAllTransmitters(Set<T> newTransmitters)
	{
		transmitters.addAll(newTransmitters);
	}
	
	@Override
	public void removeTransmitter(T transmitter)
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
			T aTransmitter = transmitters.iterator().next();
			
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
