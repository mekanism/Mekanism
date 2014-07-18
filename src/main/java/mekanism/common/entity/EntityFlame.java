package mekanism.common.entity;

import io.netty.buffer.ByteBuf;
import mekanism.api.Pos3D;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;

public class EntityFlame extends Entity implements IEntityAdditionalSpawnData
{
	public static final int LIFESPAN = 60;
	
	public EntityFlame(World world)
	{
		super(world);
	}
	
	public EntityFlame(EntityPlayer player)
	{
		super(player.worldObj);
		
		Pos3D playerPos = new Pos3D(player).translate(0, 1.6, 0);
		Pos3D flameVec = new Pos3D(1, 1, 1);
		
		flameVec.multiply(new Pos3D(player.getLook(90)));
		flameVec.rotateYaw(6);
		
		Pos3D mergedVec = playerPos.clone().translate(flameVec);
		setPosition(mergedVec.xPos, mergedVec.yPos, mergedVec.zPos);
		
		Pos3D motion = new Pos3D(0.2, 0.2, 0.2);
		motion.multiply(new Pos3D(player.getLookVec()));
		
		setHeading(motion);
		
		motionX = motion.xPos;
		motionY = motion.yPos;
		motionZ = motion.zPos;
	}
	
    public void setHeading(Pos3D motion)
    {
        float d = MathHelper.sqrt_double((motion.xPos * motion.xPos) + (motion.zPos * motion.zPos));
        
        prevRotationYaw = rotationYaw = (float)(Math.atan2(motion.xPos, motion.zPos) * 180.0D / Math.PI);
        prevRotationPitch = rotationPitch = (float)(Math.atan2(motion.yPos, d) * 180.0D / Math.PI);
    }
	
	@Override
	public void onUpdate()
	{
		ticksExisted++;
		
        prevPosX = posX;
        prevPosY = posY;
        prevPosZ = posZ;
        
        prevRotationPitch = rotationPitch;
        prevRotationYaw = rotationYaw;
        
        posX += motionX;
        posY += motionY;
        posZ += motionZ;
        
		if(ticksExisted > LIFESPAN)
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

	@Override
	public void writeSpawnData(ByteBuf dataStream) 
	{
		
	}

	@Override
	public void readSpawnData(ByteBuf dataStream) 
	{
		
	}
}
