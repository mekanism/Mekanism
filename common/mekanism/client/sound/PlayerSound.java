package mekanism.client.sound;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import universalelectricity.core.vector.Vector3;

public abstract class PlayerSound extends Sound
{
	/** The TileEntity this sound is associated with. */
	public EntityPlayer player;
	
	public int ticksSincePlay = 0;
	
	public PlayerSound(String id, String sound, EntityPlayer entity)
	{
		super(id, sound, entity, new Vector3(entity));
		
		player = entity;
	}
	
	@Override
	public float getMultiplier()
	{
		return Math.min(1, ((float)ticksSincePlay/20F))*0.3F;
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
		
		return true;
	}
	
	@Override
	public Vector3 getLocation()
	{
		return new Vector3(player);
	}
	
	@Override
	public void play()
	{
		super.play();
		
		ticksSincePlay = 0;
	}
}
