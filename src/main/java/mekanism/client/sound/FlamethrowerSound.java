package mekanism.client.sound;

import mekanism.client.ClientTickHandler;
import mekanism.common.item.ItemFlamethrower;
import mekanism.common.item.ItemJetpack;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public class FlamethrowerSound extends PlayerSound
{
	public boolean inUse = false;
	
	public FlamethrowerSound(String id, EntityPlayer entity)
	{
		super(id, "Flamethrower.ogg", entity);
		
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

		if(isPlaying)
		{
			ticksSincePlay++;
		}

		return true;
	}
	
	private boolean getInUse(EntityPlayer player)
	{
		ItemFlamethrower flamethrower = (ItemFlamethrower)player.getCurrentEquippedItem().getItem();
		
		return flamethrower.getInUse(player.getCurrentEquippedItem());
	}
}
