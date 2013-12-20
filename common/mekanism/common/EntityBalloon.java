package mekanism.common;

import mekanism.api.EnumColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.particle.EntityReddustFX;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import universalelectricity.core.vector.Vector3;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;

public class EntityBalloon extends Entity implements IEntityAdditionalSpawnData
{
	public EnumColor color = EnumColor.DARK_BLUE;
	
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
		
		if(worldObj.isRemote)
		{
			for(int i = 0; i < 10; i++)
			{
				try {
					Vector3 vec = new Vector3(posX + (rand.nextFloat()*.6 - 0.3), posY - 0.8 + (rand.nextFloat()*.6 - 0.3), posZ + (rand.nextFloat()*.6 - 0.3));
					
					EntityFX fx = new EntityReddustFX(worldObj, vec.x, vec.y, vec.z, 1, 0, 0, 0);
					fx.setRBGColorF(color.getColor(0), color.getColor(1), color.getColor(2));
					
					Minecraft.getMinecraft().effectRenderer.addEffect(fx);
				} catch(Exception e) {}
			}
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
}
