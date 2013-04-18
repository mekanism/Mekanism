package mekanism.client;

import net.minecraft.tileentity.TileEntity;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Implement this if your TileEntity has a specific sound.
 * @author AidanBrady
 *
 */
public interface IHasSound 
{
	/**
	 * Gets the sound path of this block's sound.
	 * @return sound path
	 */
	public String getSoundPath();
}
