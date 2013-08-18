package mekanism.api;

import java.util.Set;

import mekanism.common.IUniversalCable;

public interface ITransmitterNetwork<T, A, N>
{
	public void tick();
	
	public int getSize();
	
	public Set<A> getAcceptors(Object... data);
	
	public void removeTransmitter(T transmitter);
	
	public void refresh();
	
	public void split(T splitPoint);
	
	public void merge(N network);
	
	public void fixMessedUpNetwork(T transmitter);
	
	public void register();
	
	public void deregister();
}
