package mekanism.client.sound;

import mekanism.common.config.MekanismConfig.client;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.Sound;
import net.minecraft.client.audio.SoundEventAccessor;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
public class MekSound implements ISound 
{
    protected Sound sound;
    
    protected SoundEventAccessor soundEvent;
    
	protected AttenuationType attenuation;
	
	protected ResourceLocation soundLocation;
	
	protected float volume;
	
	protected float pitch;
	
	protected float x;
	
	protected float y;
	
	protected float z;
	
	protected boolean repeat;
	
	protected int repeatDelay;

	public MekSound(String sound) 
	{
		this(sound, 0);
	}

	public MekSound(String sound, float volume) 
	{
		this(sound, volume, 0);
	}

	public MekSound(String sound, float volume, float pitch)
	{
		this(sound, volume, pitch, false, 0);
	}

	public MekSound(String sound, float volume, float pitch, boolean repeat, int repeatDelay) 
	{
		this(sound, volume, pitch, repeat, repeatDelay, 0, 0, 0, AttenuationType.NONE);
	}

	public MekSound(String sound, float volume, float pitch, double x, double y, double z)
	{
		this(sound, volume, pitch, false, 0, x, y, z);
	}

	public MekSound(String sound, float volume, float pitch, boolean repeat, int repeatDelay, double x, double y, double z) 
	{
		this(sound, volume, pitch, repeat, repeatDelay, x, y, z, AttenuationType.LINEAR);
	}

	public MekSound(String sound, float volume, float pitch, boolean repeat, int repeatDelay, double x, double y, double z, AttenuationType attenuation) 
	{
		this(new ResourceLocation(sound), volume, pitch, repeat, repeatDelay, x, y, z, attenuation);
	}

	public MekSound(ResourceLocation sound)
	{
		this(sound, 0);
	}

	public MekSound(ResourceLocation sound, float volume)
	{
		this(sound, volume, 0);
	}

	public MekSound(ResourceLocation sound, float volume, float pitch) 
	{
		this(sound, volume, pitch, false, 0);
	}

	public MekSound(ResourceLocation sound, float volume, float pitch, boolean repeat, int repeatDelay) 
	{
		this(sound, volume, pitch, repeat, repeatDelay, 0, 0, 0, AttenuationType.NONE);
	}

	public MekSound(ResourceLocation sound, float volume, float pitch, double x, double y, double z) 
	{
		this(sound, volume, pitch, false, 0, x, y, z);
	}

	public MekSound(ResourceLocation sound, float volume, float pitch, boolean repeat, int repeatDelay, double x, double y, double z) 
	{
		this(sound, volume, pitch, repeat, repeatDelay, x, y, z, AttenuationType.LINEAR);
	}

	public MekSound(ResourceLocation resource, float v, float p, boolean rep, int delay, double xPos, double yPos, double zPos, AttenuationType att)
	{
		attenuation = att;
		soundLocation = resource;
		volume = v;
		pitch = p;
		x = (float)xPos;
		y = (float)yPos;
		z = (float)zPos;
		repeat = rep;
		repeatDelay = delay;
	}

	public MekSound(MekSound other) 
	{
		attenuation = other.attenuation;
		soundLocation = other.soundLocation;
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
	public ResourceLocation getSoundLocation()
	{
		return soundLocation;
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

	@Override
	public SoundEventAccessor createAccessor(SoundHandler handler) 
	{
        soundEvent = handler.getAccessor(soundLocation);

        if(soundEvent == null)
        {
            sound = SoundHandler.MISSING_SOUND;
        }
        else {
            sound = soundEvent.cloneEntry();
        }

        return soundEvent;
	}

	@Override
	public Sound getSound() 
	{
		return sound;
	}

	@Override
	public SoundCategory getCategory() 
	{
		return SoundCategory.BLOCKS;
	}
}
