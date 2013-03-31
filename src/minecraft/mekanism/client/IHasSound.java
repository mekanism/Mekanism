package mekanism.client;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Implement this if your TileEntity has a specific sound.
 * @author AidanBrady
 *
 */
@SideOnly(Side.CLIENT)
public interface IHasSound 
{
	/**
	 * Gets the sound.
	 * @return sound
	 */
	public Sound getSound();
	
	/**
	 * Removes the sound;
	 */
	public void removeSound();
	
	/**
	 * Ticks and updates the block's sound.
	 */
	public void updateSound();
}
