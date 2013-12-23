package mekanism.common;

import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.api.Pos3D;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.particle.EntityReddustFX;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;

public class EntityBalloon extends Entity implements IEntityAdditionalSpawnData
{
	public EnumColor color = EnumColor.DARK_BLUE;
	public Coord4D latched;
	
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
	
    public EntityBalloon(World world, double x, double y, double z, EnumColor c)
    {
        this(world);
        
        setPosition(x + 0.5F, y + 3F, z + 0.5F);
        
        prevPosX = posX;
        prevPosY = posY;
        prevPosZ = posZ;
        
        color = c;
    }
    
    public EntityBalloon(World world, Coord4D obj, EnumColor c)
    {
    	this(world);
    	
    	latched = obj;
    	setPosition(latched.xCoord + 0.5F, latched.yCoord + 2.8F, latched.zCoord + 0.5F);
    	
        prevPosX = posX;
        prevPosY = posY;
        prevPosZ = posZ;
        
    	color = c;
    }
    
    @Override
    public void onUpdate()
    {
        prevPosX = posX;
        prevPosY = posY;
        prevPosZ = posZ;
        
        if(posY > 255)
        {
        	pop();
        	return;
        }
        
        if(latched != null && latched.getBlockId(worldObj) == 0)
        {
        	latched = null;
        }
        
        if(latched == null)
        {
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
        else {
        	motionX = 0;
        	motionY = 0;
        	motionZ = 0;
        }
    }
    
    private void pop()
    {
		worldObj.playSoundAtEntity(this, "mekanism:etc.Pop", 1, 1);
		
		if(worldObj.isRemote)
		{
			for(int i = 0; i < 10; i++)
			{
				try {
					Pos3D pos = new Pos3D(posX + (rand.nextFloat()*.6 - 0.3), posY - 0.8 + (rand.nextFloat()*.6 - 0.3), posZ + (rand.nextFloat()*.6 - 0.3));
					
					EntityFX fx = new EntityReddustFX(worldObj, pos.xPos, pos.yPos, pos.zPos, 1, 0, 0, 0);
					fx.setRBGColorF(color.getColor(0), color.getColor(1), color.getColor(2));
					
					Minecraft.getMinecraft().effectRenderer.addEffect(fx);
				} catch(Exception e) {}
			}
		}
		
		setDead();
    }
	
    @Override
    public boolean canBePushed()
    {
    	return latched == null;
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
		
		if(nbtTags.hasKey("latched"))
		{
			latched = Coord4D.read(nbtTags);
		}
	}
	
	@Override
	public boolean hitByEntity(Entity entity)
	{
		pop();
		return true;
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbtTags)
	{
		nbtTags.setInteger("color", color.ordinal());
		
		if(latched != null)
		{
			latched.write(nbtTags);
		}
	}

	@Override
	public void writeSpawnData(ByteArrayDataOutput data)
	{
		data.writeDouble(posX);
		data.writeDouble(posY);
		data.writeDouble(posZ);
		
		data.writeInt(color.ordinal());
		
		if(latched != null)
		{
			data.writeBoolean(true);
			
			data.writeInt(latched.xCoord);
			data.writeInt(latched.yCoord);
			data.writeInt(latched.zCoord);
			data.writeInt(latched.dimensionId);
		}
		else {
			data.writeBoolean(false);
		}
	}

	@Override
	public void readSpawnData(ByteArrayDataInput data)
	{
		setPosition(data.readDouble(), data.readDouble(), data.readDouble());
		
		color = EnumColor.values()[data.readInt()];
		
		if(data.readBoolean())
		{
			latched = Coord4D.read(data);
		}
		else {
			latched = null;
		}
	}
	
	@Override
	public boolean isInRangeToRenderDist(double dist)
	{
		return dist <= 64;
	}
	
	@Override
    public boolean isInRangeToRenderVec3D(Vec3 par1Vec3)
    {
		return true;
    }
}
