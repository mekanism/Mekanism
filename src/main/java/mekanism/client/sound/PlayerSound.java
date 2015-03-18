package mekanism.client.sound;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public abstract class PlayerSound extends Sound implements IResettableSound
{
	public EntityPlayer player;

	boolean beginFadeOut;
	
	boolean donePlaying = true;
	
	int ticks = 0;
	
	int fadeIn;
	
	int fadeOut;
	
	float baseVolume = 0.3F;

	public PlayerSound(EntityPlayer p, ResourceLocation location)
	{
		super(location, 0.3F, 1, true, 0, (float)p.posX, (float)p.posY, (float)p.posZ, AttenuationType.LINEAR);
		player = p;
	}

	@Override
	public float getXPosF()
	{
		return (float)player.posX;
	}

	@Override
	public float getYPosF()
	{
		return (float)player.posY;
	}

	@Override
	public float getZPosF()
	{
		return (float)player.posZ;
	}

	public PlayerSound setFadeIn(int fade) 
	{
		fadeIn = Math.max(0, fade);
		return this;
	}

	public PlayerSound setFadeOut(int fade) 
	{
		fadeOut = Math.max(0, fade);
		return this;
	}

	public float getFadeInMultiplier()
	{
		return ticks >= fadeIn ? 1 : (ticks / (float)fadeIn);
	}

	public float getFadeOutMultiplier() 
	{
		return ticks >= fadeOut ? 0 : ((fadeOut - ticks) / (float)fadeOut);
	}

	@Override
	public void update()
	{
		if(!beginFadeOut)
		{
			if(ticks < fadeIn)
			{
				ticks++;
			}
			
			if(!shouldPlaySound())
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

	public abstract boolean shouldPlaySound();

	@Override
	public void reset()
	{
		donePlaying = false;
		beginFadeOut = false;
		volume = baseVolume;
		ticks = 0;
	}
}
