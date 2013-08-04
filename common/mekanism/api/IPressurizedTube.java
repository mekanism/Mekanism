package mekanism.api;

import net.minecraft.tileentity.TileEntity;

public interface IPressurizedTube 
{
	/**
	 * Whether or not this tube can transfer gas.
	 * @return if the tube can transfer gas
	 */
	public boolean canTransferGas();
	
    /**
     * Whether or not this tube can transfer gas into an adjacent tube.
     * @param tile - the adjacent tube
     * @return if this tube can transfer gas into the passed tube
     */
    public boolean canTransferGasToTube(TileEntity tile);
	
	/**
	 * Called when a gas is transferred through this tube.
	 * @param type - the type of gas transferred
	 */
	public void onTransfer(EnumGas type);
	
	/**
	 * Gets the GasNetwork currently in use by this tube segment.
	 * @return GasNetwork this cable is using
	 */
	public GasNetwork getNetwork();
	
	/**
	 * Gets the GasNetwork currently in use by this tube segment.
	 * @param createIfNull - If true, the tube will try and connect to an
	 * adjacent network, merging several if necessary, or creating a new one
	 * if none is available
	 * @return GasNetwork this cable is using
	 */
	public GasNetwork getNetwork(boolean createIfNull);
	
	/**
	 * Sets this cable segment's GasNetwork to a new value.
	 * @param network - GasNetwork to set to
	 */
	public void setNetwork(GasNetwork network);
	
	/**
	 * Refreshes the tube's GasNetwork.
	 */
	public void refreshNetwork();

	/**
	 * Remove a tube from its network.
	 */
	public void removeFromNetwork();

	/**
	 * Call this if you're worried a tube's network is messed up and you want
	 * it to try and fix itself.
	 */
	public void fixNetwork();
}
