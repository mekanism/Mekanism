package mekanism.common.transmitters;

import mekanism.api.transmitters.DynamicNetwork;
import mekanism.api.transmitters.IGridTransmitter;

public abstract class Transmitter<A, N extends DynamicNetwork<A, N>> implements IGridTransmitter<A, N>
{
	public N theNetwork = null;

	public boolean orphaned = true;

	@Override
	public N getTransmitterNetwork()
	{
		return theNetwork;
	}

	@Override
	public boolean hasTransmitterNetwork()
	{
		return !isOrphan() && getTransmitterNetwork() != null;
	}

	@Override
	public void setTransmitterNetwork(N network)
	{
		if(theNetwork == network)
		{
			return;
		}
		if(world().isRemote && theNetwork != null)
		{
			theNetwork.transmitters.remove(this);
			if(theNetwork.transmitters.isEmpty())
			{
				theNetwork.deregister();
			}
		}
		theNetwork = network;
		orphaned = theNetwork == null;
		if(world().isRemote && theNetwork != null)
		{
			theNetwork.transmitters.add(this);
		}
	}

	@Override
	public int getTransmitterNetworkSize()
	{
		return hasTransmitterNetwork() ? getTransmitterNetwork().getSize() : 0;
	}

	@Override
	public int getTransmitterNetworkAcceptorSize()
	{
		return hasTransmitterNetwork() ? getTransmitterNetwork().getAcceptorSize() : 0;
	}

	@Override
	public String getTransmitterNetworkNeeded()
	{
		return hasTransmitterNetwork() ? getTransmitterNetwork().getNeededInfo() : "No Network";
	}

	@Override
	public String getTransmitterNetworkFlow()
	{
		return hasTransmitterNetwork() ? getTransmitterNetwork().getFlowInfo() : "No Network";
	}

	@Override
	public String getTransmitterNetworkBuffer()
	{
		return hasTransmitterNetwork() ? getTransmitterNetwork().getStoredInfo() : "No Network";
	}

	@Override
	public double getTransmitterNetworkCapacity()
	{
		return hasTransmitterNetwork() ? getTransmitterNetwork().getCapacity() : getCapacity();
	}

	@Override
	public boolean isOrphan()
	{
		return orphaned;
	}

	@Override
	public void setOrphan(boolean nowOrphaned)
	{
		orphaned = nowOrphaned;
	}
}
