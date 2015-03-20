package mekanism.client.sound;

import mekanism.common.base.IHasSound;
import net.minecraft.util.ResourceLocation;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class TileSound extends Sound implements IResettableSound {

	IHasSound source;
	
	boolean beginFadeOut;
	
	boolean donePlaying = true;
	
	int ticks = 0;
	
	int fadeIn = 30;
	
	int fadeOut = 10;
	
	float baseVolume = 1.0F;

	public TileSound(IHasSound source, ISoundSource values)
	{
		this(source, values.getSoundLocation(), values.getVolume(), values.getPitch(), values.shouldRepeat(), values.getRepeatDelay(), values.getSoundPosition().xPos, values.getSoundPosition().yPos, values.getSoundPosition().zPos);
	}

	public TileSound(IHasSound source, ISoundSource values, ResourceLocation location)
	{
		this(source, location, values.getVolume(), values.getPitch(), values.shouldRepeat(), values.getRepeatDelay(), values.getSoundPosition().xPos, values.getSoundPosition().yPos, values.getSoundPosition().zPos);
	}

	public TileSound(IHasSound source, String sound, float volume, float pitch, boolean repeat, int repeatDelay, double x, double y, double z)
	{
		this(source, sound, volume, pitch, repeat, repeatDelay, x, y, z, AttenuationType.LINEAR);
	}

	public TileSound(IHasSound source, String sound, float volume, float pitch, boolean repeat, int repeatDelay, double x, double y, double z, AttenuationType attenuation) 
	{
		this(source, new ResourceLocation(sound), volume, pitch, repeat, repeatDelay, x, y, z, attenuation);
	}

	public TileSound(IHasSound source, ResourceLocation sound, float volume, float pitch, boolean repeat, int repeatDelay, double x, double y, double z) 
	{
		this(source, sound, volume, pitch, repeat, repeatDelay, x, y, z, AttenuationType.LINEAR);
	}

	public TileSound(IHasSound soundSource, ResourceLocation resource, float volume, float pitch, boolean repeat, int repeatDelay, double x, double y, double z, AttenuationType attenuation)
	{
		super(resource, volume, pitch, repeat, repeatDelay, x, y, z, attenuation);

		source = soundSource;
		sound = resource;
		baseVolume = volume;
	}

	public TileSound setFadeIn(int fade) 
	{
		fadeIn = Math.min(0, fade);
		return this;
	}

	public TileSound setFadeOut(int fade) 
	{
		fadeOut = Math.min(0, fade);
		return this;
	}

	public float getFadeInMultiplier() 
	{
		return ticks >= fadeIn ? 1 : (float)(ticks / (float)fadeIn);
	}

	public float getFadeOutMultiplier() 
	{
		return ticks >= fadeOut ? 0 : (float)((fadeOut - ticks) / (float)fadeOut);
	}

	/* ITickableSound */
	@Override
	public void update() 
	{
		if(!beginFadeOut) 
		{
			if(ticks < fadeIn)
			{
				ticks++;
			}
			
			if(!(source.shouldPlaySound() && source.getSound().sound == this))
			{
				beginFadeOut = true;
				ticks = 0;
			}
		} 
		else {
			ticks++;
		}
		
		float multiplier = beginFadeOut ? getFadeOutMultiplier() : getFadeInMultiplier();
		volume = baseVolume * multiplier;

		if(multiplier <= 0) 
		{
			donePlaying = true;
		}
	}

	@Override
	public boolean isDonePlaying()
	{
		return donePlaying;
	}

	@Override
	public void reset()
	{
		donePlaying = false;
		beginFadeOut = false;
		volume = baseVolume;
		ticks = 0;
	}
}
