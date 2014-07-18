package mekanism.common.entity;

import mekanism.api.Pos3D;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class EntityFlame extends Entity
{
	public EntityFlame(World world)
	{
		super(world);
	}
	
	public EntityFlame(EntityPlayer player)
	{
		super(player.worldObj);
		
		Pos3D playerPos = new Pos3D(player);
		Pos3D flameVec = new Pos3D(0.8, 0.8, 0.8);
		
		flameVec.multiply(new Pos3D(player.getLook(90)));
		flameVec.rotateYaw(15);
		
		Pos3D mergedVec = playerPos.clone().translate(flameVec);
		setPosition(mergedVec.xPos, mergedVec.yPos, mergedVec.zPos);
		
		Pos3D motion = new Pos3D(8, 8, 8);
		motion.multiply(new Pos3D(player.getLookVec()));
		
		motionX = motion.xPos;
		motionY = motion.yPos;
		motionZ = motion.zPos;
	}
	
	@Override
	public void onUpdate()
	{
        prevPosX = posX;
        prevPosY = posY;
        prevPosZ = posZ;
        prevRotationPitch = rotationPitch;
        prevRotationYaw = rotationYaw;
        
        posX += motionX;
        posY += motionY;
        posZ += motionZ;
        
		if(ticksExisted > 100)
		{
			setDead();
			return;
		}
	}

	@Override
	protected void entityInit() 
	{
		
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbtTags)
	{
		
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbtTags) 
	{
		
	}
}
