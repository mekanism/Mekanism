package mekanism.common.base;

import mekanism.common.tile.component.TileComponentConfig;

import net.minecraft.util.EnumFacing;

/**
 * Implement this if your TileEntity is capable of being modified by a Configurator in it's 'modify' mode.
 * @author AidanBrady
 *
 */
public interface ISideConfiguration
{
	/**
	 * Gets the tile's configuration component.
	 * @return the tile's configuration component
	 */
	public TileComponentConfig getConfig();

	/**
	 * Gets this machine's current orientation.
	 * @return machine's current orientation
	 */
	public EnumFacing getOrientation();

	/**
	 * Gets this machine's ejector.
	 * @return
	 */
	public IEjector getEjector();
}
