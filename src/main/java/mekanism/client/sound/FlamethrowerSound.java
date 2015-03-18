package mekanism.client.sound;

import mekanism.client.ClientTickHandler;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class FlamethrowerSound extends PlayerSound
{
	public boolean inUse;

	public ResourceLocation onSound;
	public ResourceLocation offSound;

	public FlamethrowerSound(EntityPlayer player)
	{
		super(player, new ResourceLocation("mekanism", "item.flamethrower.idle"));
		
		onSound = new ResourceLocation("mekanism", "item.flamethrower.active");
		offSound = new ResourceLocation("mekanism", "item.flamethrower.idle");
		inUse = ClientTickHandler.isFlamethrowerOn(player);
		sound = inUse ? onSound : offSound;
		
		setFadeIn(0);
		setFadeOut(0);
	}

	@Override
	public boolean isDonePlaying()
	{
		return donePlaying;
	}

	@Override
	public boolean shouldPlaySound()
	{
		return true;
	}

	@Override
	public float getVolume()
	{
		return super.getVolume() * (inUse ? 2 : 1);
	}

	@Override
	public void update()
	{
		if(!ClientTickHandler.hasFlamethrower(player))
		{
			donePlaying = true;
			return;
		}
		
		if(inUse != ClientTickHandler.isFlamethrowerOn(player))
		{
			inUse = ClientTickHandler.isFlamethrowerOn(player);
			sound = inUse ? onSound : offSound;
			donePlaying = true;
		}
	}
}
