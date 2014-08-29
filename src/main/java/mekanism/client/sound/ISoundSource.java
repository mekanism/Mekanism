package mekanism.client.sound;

import mekanism.api.Pos3D;

import net.minecraft.client.audio.ISound.AttenuationType;
import net.minecraft.util.ResourceLocation;

public interface ISoundSource
{
	public ResourceLocation getSoundLocation();

	public float getVolume();

	public float getPitch();

	public Pos3D getSoundPosition();

	public boolean shouldRepeat();

	public int getRepeatDelay();

	public AttenuationType getAttenuation();
}
