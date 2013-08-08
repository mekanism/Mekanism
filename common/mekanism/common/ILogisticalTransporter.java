package mekanism.common;

public interface ILogisticalTransporter 
{
	/**
	 * Gets the InventoryNetwork currently in use by this transporter segment.
	 * @return InventoryNetwork this transporter is using
	 */
	public InventoryNetwork getNetwork();
	
	/**
	 * Gets the InventoryNetwork currently in use by this transporter segment.
	 * @param createIfNull - If true, the transporter will try and connect to an
	 * adjacent network, merging several if necessary, or creating a new one
	 * if none is available
	 * @return InventoryNetwork this transporter is using
	 */
	public InventoryNetwork getNetwork(boolean createIfNull);
	
	/**
	 * Sets this transporter segment's InventoryNetwork to a new value.
	 * @param network - InventoryNetwork to set to
	 */
	public void setNetwork(InventoryNetwork network);
	
	/**
	 * Refreshes the transporter's InventoryNetwork.
	 */
	public void refreshNetwork();
	
	/**
	 * Remove a transporter from its network.
	 */
	public void removeFromNetwork();

	/**
	 * Call this if you're worried a transporter's network is messed up and you want
	 * it to try and fix itself.
	 */
	public void fixNetwork();
}
