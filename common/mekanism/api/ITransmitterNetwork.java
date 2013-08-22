package mekanism.api;

import java.util.Set;

public interface ITransmitterNetwork<A, N>
{
	public void tick();
	
	public int getSize();
	
	public Set<A> getAcceptors(Object... data);
	
	public void removeTransmitter(ITransmitter<N> transmitter);
	
	public void refresh();
	
	public void split(ITransmitter<N> splitPoint);
	
	public void merge(N network);
	
	public void fixMessedUpNetwork(ITransmitter<N> transmitter);
	
	public void register();
	
	public void deregister();
	
	public void setFixed(boolean value);
}
