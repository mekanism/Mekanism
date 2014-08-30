package mekanism.client.sound;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

public abstract class PlayerSound extends Sound implements IResettableSound
{
	public EntityPlayer player;

	boolean beginFadeOut;
	boolean donePlaying = true;
	int ticks = 0;
	int fadeIn;
	int fadeOut;
	float baseVolume = 0.3F;


	public PlayerSound(EntityPlayer player, ResourceLocation location)
	{
		super(location, 0.3F, 1, true, 0, (float) player.posX, (float) player.posY, (float) player.posZ, AttenuationType.LINEAR);
		this.player = player;
	}

	@Override
	public float getXPosF()
	{

		return (float) player.posX;
	}

	@Override
	public float getYPosF()
	{

		return (float) player.posY;
	}

	@Override
	public float getZPosF()
	{

		return (float) player.posZ;
	}

	public PlayerSound setFadeIn(int fadeIn) {

		this.fadeIn = Math.min(0, fadeIn);
		return this;
	}

	public PlayerSound setFadeOut(int fadeOut) {

		this.fadeOut = Math.min(0, fadeOut);
		return this;
	}

	public float getFadeInMultiplier() {

		return ticks >= fadeIn ? 1 : (ticks / (float) fadeIn);
	}

	public float getFadeOutMultiplier() {

		return ticks >= fadeOut ? 0 : ((fadeOut - ticks) / (float) fadeOut);
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
		} else
		{
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
