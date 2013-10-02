package mekanism.api.transmitters;

import java.util.Set;

public interface ITransmitterNetwork<A, N extends DynamicNetwork<A, N, D>, D>
{
	public void tick();
	
	public int getSize();
	
	public int getAcceptorSize();
	
	public String getNeeded();
	
	public String getFlow();
	
	public Set<A> getAcceptors(Object... data);
	
	public void removeTransmitter(ITransmitter<N, D> transmitter);
	
	public void refresh();
	
	public void split(ITransmitter<N, D> splitPoint);
	
	public void merge(N network);
	
	public void fixMessedUpNetwork(ITransmitter<N, D> transmitter);
	
	public void register();
	
	public void deregister();
	
	public void setFixed(boolean value);
	
	public TransmissionType getTransmissionType();
}
