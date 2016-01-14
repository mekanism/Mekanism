package mekanism.common.base;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
