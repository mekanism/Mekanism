package mekanism.common;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import mekanism.api.EnumColor;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;

public class EntityBalloon extends Entity implements IEntityAdditionalSpawnData
{
	public EnumColor color = EnumColor.DARK_RED;
	
	public EntityBalloon(World world)
	{
		super(world);
		
		ignoreFrustumCheck = true;
        preventEntitySpawning = true;
		setPosition(posX + 0.5F, posY + 3F, posZ + 0.5F);
		yOffset = height / 2.0F;
        setSize(0.25F, 0.25F);
        motionY = 0.04;
	}
	
    public EntityBalloon(World world, double x, double y, double z)
    {
        this(world);
        
        setPosition(x + 0.5F, y + 3F, z + 0.5F);
        
        prevPosX = x;
        prevPosY = y;
        prevPosZ = z;
    }
    
    @Override
    public void onUpdate()
    {
        prevPosX = posX;
        prevPosY = posY;
        prevPosZ = posZ;
        
        motionY = Math.min(motionY*1.02F, 0.2F);
        
        moveEntity(motionX, motionY, motionZ);
        
        motionX *= 0.98;
        motionZ *= 0.98;

        if(onGround)
        {
            motionX *= 0.7;
            motionZ *= 0.7;
        }
        
        if(motionY == 0)
        {
        	motionY = 0.04;
        }
    }
	
    @Override
    public boolean canBePushed()
    {
    	return true;
    }
    
    @Override
    public boolean canBeCollidedWith()
    {
        return !isDead;
    }
    
    @Override
    protected boolean canTriggerWalking()
    {
        return false;
    }

	@Override
	protected void entityInit() {}

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbtTags)
	{
		color = EnumColor.values()[nbtTags.getInteger("color")];
	}
	
	@Override
	public boolean hitByEntity(Entity entity)
	{
		worldObj.playSoundAtEntity(this, "mekanism:etc.Pop", 1, 1);
		
		for(int i = 0; i < 10; i++)
		{
			worldObj.spawnParticle("reddust", posX + (rand.nextFloat()*.6 - 0.3), posY - 0.8 + (rand.nextFloat()*.6 - 0.3), posZ + (rand.nextFloat()*.6 - 0.3), 0, 0, 0);
		}
		
		setDead();
		return true;
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbtTags)
	{
		nbtTags.setInteger("color", color.ordinal());
	}

	@Override
	public void writeSpawnData(ByteArrayDataOutput data)
	{
		data.writeDouble(posX);
		data.writeDouble(posY);
		data.writeDouble(posZ);
		
		data.writeInt(color.ordinal());
	}

	@Override
	public void readSpawnData(ByteArrayDataInput data)
	{
		setPosition(data.readDouble(), data.readDouble(), data.readDouble());
		
		color = EnumColor.values()[data.readInt()];
	}
	
    public boolean isInRangeToRenderDist(double par1)
    {
    	return true;
    }
    
    public boolean shouldRenderInPass(int pass)
    {
    	return true;
    }
}
