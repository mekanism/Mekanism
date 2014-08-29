package mekanism.client.sound;

import net.minecraft.client.audio.ITickableSound;

public interface IResettableSound extends ITickableSound
{
	public void reset();
}
