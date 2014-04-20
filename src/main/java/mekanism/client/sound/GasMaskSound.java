package mekanism.client.sound;

import mekanism.client.ClientTickHandler;
import mekanism.common.item.ItemGasMask;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public class GasMaskSound extends PlayerSound
{
	public GasMaskSound(String id, EntityPlayer entity)
	{
		super(id, "GasMask.ogg", entity);
	}

	@Override
	public boolean update(World world)
	{
		if(!super.update(world))
		{
			return false;
		}
		else if(!hasGasMask(player))
		{
			return false;
		}
		else {
			if(ClientTickHandler.isGasMaskOn(player) != isPlaying)
			{
				if(ClientTickHandler.isGasMaskOn(player))
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

	private boolean hasGasMask(EntityPlayer player)
	{
		return player.inventory.armorInventory[3] != null && player.inventory.armorInventory[3].getItem() instanceof ItemGasMask;
	}
}
