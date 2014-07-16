package mekanism.client.sound;

import mekanism.client.ClientTickHandler;
import mekanism.common.item.ItemFlamethrower;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public class FlamethrowerSound extends PlayerSound
{
	public boolean inUse = false;
	
	public FlamethrowerSound(String id, EntityPlayer entity)
	{
		super(id, getSound(getInUse(entity)), SoundHandler.CHANNEL_FLAMETHROWER, entity);
		
		inUse = getInUse(entity);
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
		else if(inUse != getInUse(player))
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
	
	private static boolean getInUse(EntityPlayer player)
	{
		ItemFlamethrower flamethrower = (ItemFlamethrower)player.getCurrentEquippedItem().getItem();
		
		return flamethrower.getInUse(player.getCurrentEquippedItem());
	}
	
	private static String getSound(boolean inUse)
	{
		return inUse ? "FlamethrowerActive.ogg" : "FlamethrowerIdle.ogg";
	}
}
