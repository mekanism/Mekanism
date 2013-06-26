package mekanism.common;

import net.minecraft.tileentity.TileEntity;

/**
 * Implement this in your TileEntity class if the block can transfer energy as a Universal Cable.
 * @author AidanBrady
 *
 */
public interface IUniversalCable 
{
	/**
	 * Whether or not this cable can transfer energy.
	 * @return if the cable can transfer energy
	 */
	public boolean canTransferEnergy();
	
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
