package mekanism.common;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class EntityObsidianTNT extends Entity
{
    /** How long the fuse is */
    public int fuse;
    private boolean hasExploded = false;

    public EntityObsidianTNT(World par1World)
    {
        super(par1World);
        fuse = 0;
        preventEntitySpawning = true;
        setSize(0.98F, 0.98F);
        yOffset = height / 2.0F;
    }

    public EntityObsidianTNT(World par1World, double par2, double par4, double par6)
    {
        this(par1World);
        setPosition(par2, par4, par6);
        float var8 = (float)(Math.random() * Math.PI * 2);
        motionX = (double)(-((float)Math.sin((double)var8)) * 0.02F);
        motionY = 0.2;
        motionZ = (double)(-((float)Math.cos((double)var8)) * 0.02F);
        fuse = Mekanism.ObsidianTNTDelay;
        prevPosX = par2;
        prevPosY = par4;
        prevPosZ = par6;
    }

    @Override
    protected void entityInit() {}

    @Override
    protected boolean canTriggerWalking()
    {
        return false;
    }

    @Override
    public boolean canBeCollidedWith()
    {
        return !isDead;
    }
    
    @Override
    public void onUpdate()
    {
        prevPosX = posX;
        prevPosY = posY;
        prevPosZ = posZ;
        motionY -= 0.04;
        moveEntity(motionX, motionY, motionZ);
        motionX *= 0.98;
        motionY *= 0.98;
        motionZ *= 0.98;

        if (onGround)
        {
            motionX *= 0.7;
            motionZ *= 0.7;
            motionY *= -0.5;
        }

        if (fuse-- <= 0)
        {
            if (!worldObj.isRemote)
            {
                setDead();
                explode();
            }
            else
            {
            	if(hasExploded)
            	{
            		setDead();
            	}
            	else {
            		worldObj.spawnParticle("lava", posX, posY + 0.5D, posZ, 0.0D, 0.0D, 0.0D);
            	}
            }
        }
        else
        {
        	worldObj.spawnParticle("lava", posX, posY + 0.5D, posZ, 0.0D, 0.0D, 0.0D);
        }
    }

    private void explode()
    {
        worldObj.createExplosion((Entity)null, posX, posY, posZ, Mekanism.ObsidianTNTBlastRadius, true);
        hasExploded = true;
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound par1NBTTagCompound)
    {
        par1NBTTagCompound.setByte("Fuse", (byte)fuse);
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound par1NBTTagCompound)
    {
        fuse = par1NBTTagCompound.getByte("Fuse");
    }

    @Override
    public float getShadowSize()
    {
        return 0.0F;
    }
}
