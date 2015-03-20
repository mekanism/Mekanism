package mekanism.common.base;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Implement this if your TileEntity has a specific sound.
 * @author AidanBrady
 *
 */
public interface IHasSound
{
	@SideOnly(Side.CLIENT)
	public SoundWrapper getSound();

	@SideOnly(Side.CLIENT)
	public boolean shouldPlaySound();
}
