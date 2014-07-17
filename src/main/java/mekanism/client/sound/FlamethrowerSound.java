package mekanism.client.sound;

import mekanism.client.ClientTickHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public class FlamethrowerSound extends PlayerSound
{
	public boolean inUse = false;
	
	public FlamethrowerSound(String id, EntityPlayer entity)
	{
		super(id, getSound(ClientTickHandler.isFlamethrowerOn(entity)), SoundHandler.CHANNEL_FLAMETHROWER, entity);
		
		inUse = ClientTickHandler.isFlamethrowerOn(entity);
	}

	@Override
	public boolean update(World world)
	{
		if(!super.update(world))
		{
			return false;
		}
		else if(!ClientTickHandler.hasFlamethrower(player))
		{
			return false;
		}
		else if(inUse != ClientTickHandler.isFlamethrowerOn(player))
		{
			return false;
		}
		
		if(!isPlaying)
		{
			play();
		}

		ticksSincePlay++;

		return true;
	}
	
	@Override
	public float getMultiplier()
	{
		return super.getMultiplier() * (inUse ? 2 : 1);
	}
	
	@Override
	public boolean doGradualEffect()
	{
		return false;
	}
	
	private static String getSound(boolean inUse)
	{
		return inUse ? "FlamethrowerActive.ogg" : "FlamethrowerIdle.ogg";
	}
}
