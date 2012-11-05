package mekanism.common;

import java.util.List;
import net.minecraft.src.*;
import java.util.Random;

public abstract class EntityProjectile extends Entity
{
    protected int xTile;
    protected int yTile;
    protected int zTile;
    protected int inTile;
    protected int inData;
    protected boolean inGround;
    public boolean doesArrowBelongToPlayer;
    public int arrowShake;
    public Entity shootingEntity;
    protected int ticksInGround;
    protected int ticksInAir;
    public boolean beenInGround;
    public boolean isCritical;

    public EntityProjectile(World world)
    {
        super(world);
        xTile = -1;
        yTile = -1;
        zTile = -1;
        inTile = 0;
        inData = 0;
        inGround = false;
        doesArrowBelongToPlayer = false;
        isCritical = false;
        arrowShake = 0;
        ticksInAir = 0;
        yOffset = 0.0F;
        setSize(0.5F, 0.5F);
    }

    protected void entityInit()
    {
    }

    public void setArrowHeading(double d, double d1, double d2, float f, float f1)
    {
        float f2 = MathHelper.sqrt_double(d * d + d1 * d1 + d2 * d2);
        d /= f2;
        d1 /= f2;
        d2 /= f2;
        d += rand.nextGaussian() * 0.0074999999999999997D * (double)f1;
        d1 += rand.nextGaussian() * 0.0074999999999999997D * (double)f1;
        d2 += rand.nextGaussian() * 0.0074999999999999997D * (double)f1;
        d *= f;
        d1 *= f;
        d2 *= f;
        motionX = d;
        motionY = d1;
        motionZ = d2;
        float f3 = MathHelper.sqrt_double(d * d + d2 * d2);
        prevRotationYaw = rotationYaw = (float)((Math.atan2(d, d2) * 180D) / Math.PI);
        prevRotationPitch = rotationPitch = (float)((Math.atan2(d1, f3) * 180D) / Math.PI);
        ticksInGround = 0;
    }

    /**
     * Sets the velocity to the args. Args: x, y, z
     */
    public void setVelocity(double d, double d1, double d2)
    {
        motionX = d;
        motionY = d1;
        motionZ = d2;

        if (prevRotationPitch == 0.0F && prevRotationYaw == 0.0F)
        {
            if (aimRotation())
            {
                float f = MathHelper.sqrt_double(d * d + d2 * d2);
                prevRotationYaw = rotationYaw = (float)((Math.atan2(d, d2) * 180D) / Math.PI);
                prevRotationPitch = rotationPitch = (float)((Math.atan2(d1, f) * 180D) / Math.PI);
                prevRotationPitch = rotationPitch;
                prevRotationYaw = rotationYaw;
            }

            setLocationAndAngles(posX, posY, posZ, rotationYaw, rotationPitch);
            ticksInGround = 0;
        }
    }

    /**
     * Called to update the entity's position/logic.
     */
    public void onUpdate()
    {
        super.onUpdate();

        if (aimRotation() && prevRotationPitch == 0.0F && prevRotationYaw == 0.0F)
        {
            float f = MathHelper.sqrt_double(motionX * motionX + motionZ * motionZ);
            prevRotationYaw = rotationYaw = (float)((Math.atan2(motionX, motionZ) * 180D) / Math.PI);
            prevRotationPitch = rotationPitch = (float)((Math.atan2(motionY, f) * 180D) / Math.PI);
        }

        int i = worldObj.getBlockId(xTile, yTile, zTile);

        if (i > 0)
        {
            Block.blocksList[i].setBlockBoundsBasedOnState(worldObj, xTile, yTile, zTile);
            AxisAlignedBB axisalignedbb = Block.blocksList[i].getCollisionBoundingBoxFromPool(worldObj, xTile, yTile, zTile);

            if (axisalignedbb != null && axisalignedbb.isVecInside(Vec3.createVectorHelper(posX, posY, posZ)))
            {
                inGround = true;
            }
        }

        if (arrowShake > 0)
        {
            arrowShake--;
        }

        if (inGround)
        {
            int j = worldObj.getBlockId(xTile, yTile, zTile);
            int k = worldObj.getBlockMetadata(xTile, yTile, zTile);

            if (j != inTile || k != inData)
            {
                inGround = false;
                motionX *= rand.nextFloat() * 0.2F;
                motionY *= rand.nextFloat() * 0.2F;
                motionZ *= rand.nextFloat() * 0.2F;
                ticksInGround = 0;
                ticksInAir = 0;
            }
            else
            {
                ticksInGround++;

                if (ticksInGround >= (doesArrowBelongToPlayer ? 200 : 800))
                {
                    setDead();
                }
            }

            return;
        }

        ticksInAir++;
        Vec3 vec3d = Vec3.createVectorHelper(posX, posY, posZ);
        Vec3 vec3d1 = Vec3.createVectorHelper(posX + motionX, posY + motionY, posZ + motionZ);
        MovingObjectPosition movingobjectposition = worldObj.rayTraceBlocks_do_do(vec3d, vec3d1, false, true);
        vec3d = Vec3.createVectorHelper(posX, posY, posZ);
        vec3d1 = Vec3.createVectorHelper(posX + motionX, posY + motionY, posZ + motionZ);

        if (movingobjectposition != null)
        {
            vec3d1 = Vec3.createVectorHelper(movingobjectposition.hitVec.xCoord, movingobjectposition.hitVec.yCoord, movingobjectposition.hitVec.zCoord);
        }

        Entity entity = null;
        List list = worldObj.getEntitiesWithinAABBExcludingEntity(this, boundingBox.addCoord(motionX, motionY, motionZ).expand(1.0D, 1.0D, 1.0D));
        double d = 0.0D;

        for (int l = 0; l < list.size(); l++)
        {
            Entity entity1 = (Entity)list.get(l);

            if (!entity1.canBeCollidedWith() || entity1 == shootingEntity && ticksInAir < 5)
            {
                continue;
            }

            float f4 = 0.3F;
            AxisAlignedBB axisalignedbb1 = entity1.boundingBox.expand(f4, f4, f4);
            MovingObjectPosition movingobjectposition1 = axisalignedbb1.calculateIntercept(vec3d, vec3d1);

            if (movingobjectposition1 == null)
            {
                continue;
            }

            double d1 = vec3d.distanceTo(movingobjectposition1.hitVec);

            if (d1 < d || d == 0.0D)
            {
                entity = entity1;
                d = d1;
            }
        }

        if (entity != null)
        {
            movingobjectposition = new MovingObjectPosition(entity);
        }

        if (movingobjectposition != null)
        {
            if (movingobjectposition.entityHit != null)
            {
                onEntityHit(movingobjectposition.entityHit);
            }
            else
            {
                onGroundHit(movingobjectposition);
            }
        }

        if (isCritical)
        {
            for (int i1 = 0; i1 < 2; i1++)
            {
                worldObj.spawnParticle("crit", posX + (motionX * (double)i1) / 4D, posY + (motionY * (double)i1) / 4D, posZ + (motionZ * (double)i1) / 4D, -motionX, -motionY + 0.20000000000000001D, -motionZ);
            }
        }

        posX += motionX;
        posY += motionY;
        posZ += motionZ;

        if (aimRotation())
        {
            float f1 = MathHelper.sqrt_double(motionX * motionX + motionZ * motionZ);
            rotationYaw = (float)((Math.atan2(motionX, motionZ) * 180D) / Math.PI);

            for (rotationPitch = (float)((Math.atan2(motionY, f1) * 180D) / Math.PI); rotationPitch - prevRotationPitch < -180F; prevRotationPitch -= 360F) { }

            for (; rotationPitch - prevRotationPitch >= 180F; prevRotationPitch += 360F) { }

            for (; rotationYaw - prevRotationYaw < -180F; prevRotationYaw -= 360F) { }

            for (; rotationYaw - prevRotationYaw >= 180F; prevRotationYaw += 360F) { }

            rotationPitch = prevRotationPitch + (rotationPitch - prevRotationPitch) * 0.2F;
            rotationYaw = prevRotationYaw + (rotationYaw - prevRotationYaw) * 0.2F;
        }

        float f2 = getAirResistance();
        float f3 = getGravity();

        if (isInWater())
        {
            beenInGround = true;

            for (int j1 = 0; j1 < 4; j1++)
            {
                float f5 = 0.25F;
                worldObj.spawnParticle("bubble", posX - motionX * (double)f5, posY - motionY * (double)f5, posZ - motionZ * (double)f5, motionX, motionY, motionZ);
            }

            f2 = 0.8F;
        }

        motionX *= f2;
        motionY *= f2;
        motionZ *= f2;
        motionY -= f3;
        setPosition(posX, posY, posZ);
    }

    public final double getTotalVelocity()
    {
        return Math.sqrt(motionX * motionX + motionY * motionY + motionZ * motionZ);
    }

    public boolean aimRotation()
    {
        return true;
    }

    public void onEntityHit(Entity entity)
    {
        bounceBack();
    }

    public void onGroundHit(MovingObjectPosition movingobjectposition)
    {
        xTile = movingobjectposition.blockX;
        yTile = movingobjectposition.blockY;
        zTile = movingobjectposition.blockZ;
        inTile = worldObj.getBlockId(xTile, yTile, zTile);
        inData = worldObj.getBlockMetadata(xTile, yTile, zTile);
        motionX = (float)(movingobjectposition.hitVec.xCoord - posX);
        motionY = (float)(movingobjectposition.hitVec.yCoord - posY);
        motionZ = (float)(movingobjectposition.hitVec.zCoord - posZ);
        float f = MathHelper.sqrt_double(motionX * motionX + motionY * motionY + motionZ * motionZ);
        posX -= (motionX / (double)f) * 0.050000000000000003D;
        posY -= (motionY / (double)f) * 0.050000000000000003D;
        posZ -= (motionZ / (double)f) * 0.050000000000000003D;
        inGround = true;
        beenInGround = true;
        isCritical = false;
        arrowShake = getMaxArrowShake();
        playHitSound();
    }

    protected void bounceBack()
    {
        motionX *= -0.10000000000000001D;
        motionY *= -0.10000000000000001D;
        motionZ *= -0.10000000000000001D;
        rotationYaw += 180F;
        prevRotationYaw += 180F;
        ticksInAir = 0;
    }

    public ItemStack getPickupItem()
    {
        return null;
    }

    public float getAirResistance()
    {
        return 0.99F;
    }

    public float getGravity()
    {
        return 0.05F;
    }

    public int getMaxArrowShake()
    {
        return 7;
    }

    public void playHitSound()
    {
    }

    /**
     * Called by a player entity when they collide with an entity
     */
    public void onCollideWithPlayer(EntityPlayer entityplayer)
    {
        if (worldObj.isRemote)
        {
            return;
        }

        ItemStack itemstack = getPickupItem();

        if (itemstack == null)
        {
            return;
        }

        if (inGround && doesArrowBelongToPlayer && arrowShake <= 0 && entityplayer.inventory.addItemStackToInventory(itemstack))
        {
            worldObj.playSoundAtEntity(this, "random.pop", 0.2F, ((rand.nextFloat() - rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
            entityplayer.onItemPickup(this, 1);
            setDead();
        }
    }

    public float getShadowSize()
    {
        return 0.0F;
    }

    /**
     * (abstract) Protected helper method to write subclass entity data to NBT.
     */
    public void writeEntityToNBT(NBTTagCompound nbttagcompound)
    {
        nbttagcompound.setShort("xTile", (short)xTile);
        nbttagcompound.setShort("yTile", (short)yTile);
        nbttagcompound.setShort("zTile", (short)zTile);
        nbttagcompound.setByte("inTile", (byte)inTile);
        nbttagcompound.setByte("inData", (byte)inData);
        nbttagcompound.setByte("shake", (byte)arrowShake);
        nbttagcompound.setBoolean("inGround", inGround);
        nbttagcompound.setBoolean("player", doesArrowBelongToPlayer);
        nbttagcompound.setBoolean("crit", isCritical);
        nbttagcompound.setBoolean("beenInGround", beenInGround);
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    public void readEntityFromNBT(NBTTagCompound nbttagcompound)
    {
        xTile = nbttagcompound.getShort("xTile");
        yTile = nbttagcompound.getShort("yTile");
        zTile = nbttagcompound.getShort("zTile");
        inTile = nbttagcompound.getByte("inTile") & 0xff;
        inData = nbttagcompound.getByte("inData") & 0xff;
        arrowShake = nbttagcompound.getByte("shake") & 0xff;
        inGround = nbttagcompound.getBoolean("inGround");
        doesArrowBelongToPlayer = nbttagcompound.getBoolean("player");
        isCritical = nbttagcompound.getBoolean("crit");
        beenInGround = nbttagcompound.getBoolean("beenInGrond");
    }
}
