package mekanism.api;

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
	public boolean canTransferEnergy(TileEntity fromTile);
}
