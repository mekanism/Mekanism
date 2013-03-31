package mekanism.api;

import java.util.ArrayList;

/**
 * Implement this if your TileEntity is capable of being modified by a Configurator in it's 'modify' mode.
 * @author AidanBrady
 *
 */
public interface IConfigurable 
{
	/**
	 * Gets an ArrayList of side data this machine contains.
	 * @return
	 */
	public ArrayList<SideData> getSideData();
	
	/**
	 * Gets this machine's configuration as a byte[] -- each byte matching with the index of the defined SideData.
	 * @return
	 */
	public byte[] getConfiguration();
	
	/**
	 * Gets this machine's current orientation.
	 * @return
	 */
	public int getOrientation();
}
