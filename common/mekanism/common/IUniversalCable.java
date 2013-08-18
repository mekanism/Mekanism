package mekanism.common;

import mekanism.api.ITransmitter;

/**
 * Implement this in your TileEntity class if the block can transfer energy as a Universal Cable.
 * @author AidanBrady
 *
 */
public interface IUniversalCable extends ITransmitter<EnergyNetwork>
{
	/**
	 * Sets a Universal Cable's energy scale to a new value.
	 * @param energyScale - energy scale to set
	 */
	public void setCachedEnergy(double energyScale);
}
