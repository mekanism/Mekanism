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
	 * @return EnergyNetwork this cable is using
	 */
	public EnergyNetwork getNetwork();
	
	/**
	 * Sets this cable segment's EnergyNetwork to a new value.
	 * @param network - EnergyNetwork to set to
	 */
	public void setNetwork(EnergyNetwork network);
	
	/**
	 * Refreshes the cable's EnergyNetwork.
	 */
	public void refreshNetwork();
}
