package mekanism.client.sound;

import mekanism.client.ClientPlayerTickHandler;
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
		if(player.isDead)
		{
			return false;
		}
		else if(!world.loadedEntityList.contains(player))
		{
			return false;
		}
		else if(!hasGasMask(player))
		{
			return false;
		}
		else {
			if(ClientPlayerTickHandler.isGasMaskOn(player) != isPlaying)
			{
				if(ClientPlayerTickHandler.isGasMaskOn(player))
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
