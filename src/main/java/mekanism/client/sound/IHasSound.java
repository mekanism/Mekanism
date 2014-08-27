package mekanism.client.sound;

import net.minecraft.client.audio.ISound;

/**
 * Implement this if your TileEntity has a specific sound.
 * @author AidanBrady
 *
 */
public interface IHasSound
{
	public ISound getSound();

	public boolean shouldPlaySound();
}
