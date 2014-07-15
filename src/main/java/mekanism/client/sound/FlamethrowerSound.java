package mekanism.client.sound;

import mekanism.client.ClientTickHandler;
import mekanism.common.item.ItemFlamethrower;
import mekanism.common.item.ItemJetpack;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public class FlamethrowerSound extends PlayerSound
{
	public byte type = 0;
	
	public FlamethrowerSound(String id, EntityPlayer entity)
	{
		super(id, "Jetpack.ogg", entity);
	}

	@Override
	public boolean update(World world)
	{
		if(!super.update(world))
		{
			return false;
		}
		else if(!hasFlamethrower(player))
		{
			return false;
		}
		else {
			if(ClientTickHandler.isJetpackOn(player) != isPlaying)
			{
				if(ClientTickHandler.isJetpackOn(player))
				{
					play();
				}
				else {
					stopLoop();
				}
			}
		}

		if(isPlaying)
		{
			ticksSincePlay++;
		}

		return true;
	}

	private boolean hasFlamethrower(EntityPlayer player)
	{
		if(player.getCurrentEquippedItem() != null && player.getCurrentEquippedItem().getItem() instanceof ItemJetpack)
		{
			ItemFlamethrower flamethrower = (ItemFlamethrower)player.getCurrentEquippedItem().getItem();
			
			if(flamethrower.getGas(player.getCurrentEquippedItem()) != null)
			{
				return true;
			}
		}
		
		return false;
	}
}
