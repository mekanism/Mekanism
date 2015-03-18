package mekanism.common.base;

import net.minecraft.client.audio.ISound;
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
	public ISound getSound();

	@SideOnly(Side.CLIENT)
	public boolean shouldPlaySound();
}
