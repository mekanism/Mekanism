package mekanism.api.transmitters;

import java.util.List;
import java.util.Set;

import mekanism.api.transmitters.DynamicNetwork.NetworkFinder;

public interface ITransmitterNetwork<A, N extends DynamicNetwork<A, N>>
{
	public void tick();
	
	public int getSize();
	
	public int getAcceptorSize();
	
	public String getNeeded();
	
	public String getFlow();
	
	public Set<A> getAcceptors(Object... data);
	
	public void removeTransmitter(ITransmitter<N> transmitter);
	
	public void refresh();
	
	public void split(ITransmitter<N> splitPoint);
	
	public void merge(N network);
	
	public void fixMessedUpNetwork(ITransmitter<N> transmitter);
	
	public void register();
	
	public void deregister();
	
	public void setFixed(boolean value);
	
	public TransmissionType getTransmissionType();
	
	/**
	 * Gets a network's meta value right before it is split. This will then be passed onto "onNewFromSplit()" in
	 * every single new network that is created from the original split.
	 * @param size - the amount of new networks that are being created
	 * @return meta obj
	 */
	public void onNetworksCreated(List<N> networks);
}
