package mekanism.client.sound;

import mekanism.client.ClientTickHandler;
import mekanism.common.item.ItemJetpack;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public class JetpackSound extends PlayerSound
{
	public JetpackSound(String id, EntityPlayer entity)
	{
		super(id, "Jetpack.ogg", SoundHandler.CHANNEL_JETPACK, entity);
	}

	@Override
	public boolean update(World world)
	{
		if(!super.update(world))
		{
			return false;
		}
		else if(!hasJetpack(player))
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

	private boolean hasJetpack(EntityPlayer player)
	{
		return player.inventory.armorInventory[2] != null && player.inventory.armorInventory[2].getItem() instanceof ItemJetpack;
	}
}
