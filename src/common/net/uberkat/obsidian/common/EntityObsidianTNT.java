package net.uberkat.obsidian.common;

import net.minecraft.src.*;

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
        float var8 = (float)(Math.random() * Math.PI * 2.0D);
        motionX = (double)(-((float)Math.sin((double)var8)) * 0.02F);
        motionY = 0.20000000298023224D;
        motionZ = (double)(-((float)Math.cos((double)var8)) * 0.02F);
        fuse = ObsidianIngotsCore.ObsidianTNTDelay;
        prevPosX = par2;
        prevPosY = par4;
        prevPosZ = par6;
    }

    protected void entityInit() {}

    /**
     * returns if this entity triggers Block.onEntityWalking on the blocks they walk on. used for spiders and wolves to
     * prevent them from trampling crops
     */
    protected boolean canTriggerWalking()
    {
        return false;
    }

    /**
     * Returns true if other Entities should be prevented from moving through this Entity.
     */
    public boolean canBeCollidedWith()
    {
        return !isDead;
    }

    /**
     * Called to update the entity's position/logic.
     */
    public void onUpdate()
    {
        prevPosX = posX;
        prevPosY = posY;
        prevPosZ = posZ;
        motionY -= 0.03999999910593033D;
        moveEntity(motionX, motionY, motionZ);
        motionX *= 0.9800000190734863D;
        motionY *= 0.9800000190734863D;
        motionZ *= 0.9800000190734863D;

        if (onGround)
        {
            motionX *= 0.699999988079071D;
            motionZ *= 0.699999988079071D;
            motionY *= -0.5D;
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
        worldObj.createExplosion((Entity)null, posX, posY, posZ, ObsidianIngotsCore.ObsidianTNTBlastRadius);
        hasExploded = true;
    }

    /**
     * (abstract) Protected helper method to write subclass entity data to NBT.
     */
    protected void writeEntityToNBT(NBTTagCompound par1NBTTagCompound)
    {
        par1NBTTagCompound.setByte("Fuse", (byte)fuse);
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    protected void readEntityFromNBT(NBTTagCompound par1NBTTagCompound)
    {
        fuse = par1NBTTagCompound.getByte("Fuse");
    }

    public float getShadowSize()
    {
        return 0.0F;
    }
}
