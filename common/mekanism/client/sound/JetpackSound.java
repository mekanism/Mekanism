package mekanism.client.sound;

import mekanism.client.ClientPlayerTickHandler;
import mekanism.common.item.ItemJetpack;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import universalelectricity.core.vector.Vector3;

public class JetpackSound extends PlayerSound
{
	public JetpackSound(String id, EntityPlayer entity)
	{
		super(id, "Jetpack.ogg", entity);
	}
    
	@Override
	public boolean update(World world)
	{
		if(player.isDead)
		{
			return false;
		}
		else if(player.worldObj != world)
		{
			return false;
		}
		else if(!world.loadedEntityList.contains(player))
		{
			return false;
		}
		else if(!hasJetpack(player))
		{
			return false;
		}
		else {
			if(ClientPlayerTickHandler.isJetpackOn(player) != isPlaying)
			{
				if(ClientPlayerTickHandler.isJetpackOn(player))
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
