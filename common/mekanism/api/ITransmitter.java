package mekanism.api;

public interface ITransmitter<N> 
{
	/**
	 * Gets the network currently in use by this transmitter segment.
	 * @return network this transmitter is using
	 */
	public N getNetwork();
	
	/**
	 * Gets the network currently in use by this transmitter segment.
	 * @param createIfNull - If true, the transmitter will try and connect to an
	 * adjacent network, merging several if necessary, or creating a new one
	 * if none is available
	 * @return network this transmitter is using
	 */
	public N getNetwork(boolean createIfNull);
	
	/**
	 * Sets this transmitter segment's network to a new value.
	 * @param network - network to set to
	 */
	public void setNetwork(N network);
	
	/**
	 * Refreshes the transmitter's network.
	 */
	public void refreshNetwork();
	
	/**
	 * Remove this transmitter from its network.
	 */
	public void removeFromNetwork();

	/**
	 * Call this if you're worried a transmitter's network is messed up and you want
	 * it to try and fix itself.
	 */
	public void fixNetwork();
}
