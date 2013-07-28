package mekanism.common;

/**
 * Implement this in your TileEntity class if the block can transfer energy as a Universal Cable.
 * @author AidanBrady
 *
 */
public interface IUniversalCable 
{
	/**
	 * Gets the EnergyNetwork currently in use by this cable segment.
	 * Will try to connect to adjacent networks or create a new one
	 * @return EnergyNetwork this cable is using
	 */
	public EnergyNetwork getNetwork();
	
	/**
	 * Gets the EnergyNetwork currently in use by this cable segment.
	 * @param createIfNull - If true, the cable will try and connect to an
	 * adjacent network, merging several if necessary, or creating a new one
	 * if none is available
	 * @return EnergyNetwork this cable is using
	 */
	public EnergyNetwork getNetwork(boolean createIfNull);
	
	/**
	 * Sets this cable segment's EnergyNetwork to a new value.
	 * @param network - EnergyNetwork to set to
	 */
	public void setNetwork(EnergyNetwork network);
	
	/**
	 * Refreshes the cable's EnergyNetwork.
	 */
	public void refreshNetwork();

	/**
	 * Remove a cable from its network.
	 */
	public void removeFromNetwork();

	/**
	 * Call this if you're worried a cable's network is messed up and you want
	 * it to try and fix itself.
	 */
	public void fixNetwork();

}
