package mekanism.client.sound;

import mekanism.api.MekanismConfig.client;

import net.minecraft.client.audio.ISound;
import net.minecraft.util.ResourceLocation;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Generic ISound class with lots of constructor functionality.
 * Required because - of course - Mojang has no generic that
 * lets you specify *any* arguments for this.
 *
 * Taken from CoFHLib
 *
 * @author skyboy
 *
 */
@SideOnly(Side.CLIENT)
public class Sound implements ISound 
{
	protected AttenuationType attenuation;
	
	protected ResourceLocation sound;
	
	protected float volume;
	
	protected float pitch;
	
	protected float x;
	
	protected float y;
	
	protected float z;
	
	protected boolean repeat;
	
	protected int repeatDelay;

	public Sound(String sound) 
	{
		this(sound, 0);
	}

	public Sound(String sound, float volume) 
	{
		this(sound, volume, 0);
	}

	public Sound(String sound, float volume, float pitch)
	{
		this(sound, volume, pitch, false, 0);
	}

	public Sound(String sound, float volume, float pitch, boolean repeat, int repeatDelay) 
	{
		this(sound, volume, pitch, repeat, repeatDelay, 0, 0, 0, AttenuationType.NONE);
	}

	public Sound(String sound, float volume, float pitch, double x, double y, double z)
	{
		this(sound, volume, pitch, false, 0, x, y, z);
	}

	public Sound(String sound, float volume, float pitch, boolean repeat, int repeatDelay, double x, double y, double z) 
	{
		this(sound, volume, pitch, repeat, repeatDelay, x, y, z, AttenuationType.LINEAR);
	}

	public Sound(String sound, float volume, float pitch, boolean repeat, int repeatDelay, double x, double y, double z, AttenuationType attenuation) 
	{
		this(new ResourceLocation(sound), volume, pitch, repeat, repeatDelay, x, y, z, attenuation);
	}

	public Sound(ResourceLocation sound)
	{
		this(sound, 0);
	}

	public Sound(ResourceLocation sound, float volume)
	{
		this(sound, volume, 0);
	}

	public Sound(ResourceLocation sound, float volume, float pitch) 
	{
		this(sound, volume, pitch, false, 0);
	}

	public Sound(ResourceLocation sound, float volume, float pitch, boolean repeat, int repeatDelay) 
	{
		this(sound, volume, pitch, repeat, repeatDelay, 0, 0, 0, AttenuationType.NONE);
	}

	public Sound(ResourceLocation sound, float volume, float pitch, double x, double y, double z) 
	{
		this(sound, volume, pitch, false, 0, x, y, z);
	}

	public Sound(ResourceLocation sound, float volume, float pitch, boolean repeat, int repeatDelay, double x, double y, double z) 
	{
		this(sound, volume, pitch, repeat, repeatDelay, x, y, z, AttenuationType.LINEAR);
	}

	public Sound(ResourceLocation resource, float v, float p, boolean rep, int delay, double xPos, double yPos, double zPos, AttenuationType att)
	{
		attenuation = att;
		sound = resource;
		volume = v;
		pitch = p;
		x = (float)xPos;
		y = (float)yPos;
		z = (float)zPos;
		repeat = rep;
		repeatDelay = delay;
	}

	public Sound(Sound other) 
	{
		attenuation = other.attenuation;
		sound = other.sound;
		volume = other.volume;
		pitch = other.pitch;
		x = other.x;
		y = other.y;
		z = other.z;
		repeat = other.repeat;
		repeatDelay = other.repeatDelay;
	}

	@Override
	public AttenuationType getAttenuationType() 
	{
		return attenuation;
	}

	@Override
	public ResourceLocation getPositionedSoundLocation() 
	{
		return sound;
	}

	@Override
	public float getVolume() 
	{
		return volume * client.baseSoundVolume;
	}

	@Override
	public float getPitch() 
	{
		return pitch;
	}

	@Override
	public float getXPosF() 
	{
		return x;
	}

	@Override
	public float getYPosF() 
	{
		return y;
	}

	@Override
	public float getZPosF() 
	{
		return z;
	}

	@Override
	public boolean canRepeat()
	{
		return repeat;
	}

	@Override
	public int getRepeatDelay() 
	{
		return repeatDelay;
	}
}
