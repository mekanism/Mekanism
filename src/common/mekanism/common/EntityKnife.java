package mekanism.common;

import java.util.Random;
import net.minecraft.src.*;

public class EntityKnife extends EntityProjectile
{
    protected ItemStack thrownItem;
    private int soundTimer;

    public EntityKnife(World world)
    {
        super(world);
    }

    public EntityKnife(World world, double d, double d1, double d2)
    {
        this(world);
        setPosition(d, d1, d2);
    }

    public EntityKnife(World world, EntityLiving entityliving, ItemStack itemstack)
    {
        this(world);
        thrownItem = itemstack;
        shootingEntity = entityliving;
        doesArrowBelongToPlayer = entityliving instanceof EntityPlayer;
        soundTimer = 0;
        setLocationAndAngles(entityliving.posX, entityliving.posY + (double)entityliving.getEyeHeight(), entityliving.posZ, entityliving.rotationYaw, entityliving.rotationPitch);
        posX -= MathHelper.cos((rotationYaw / 180F) * (float)Math.PI) * 0.16F;
        posY -= 0.10000000000000001D;
        posZ -= MathHelper.sin((rotationYaw / 180F) * (float)Math.PI) * 0.16F;
        setPosition(posX, posY, posZ);
        yOffset = 0.0F;
        motionX = -MathHelper.sin((rotationYaw / 180F) * (float)Math.PI) * MathHelper.cos((rotationPitch / 180F) * (float)Math.PI);
        motionZ = MathHelper.cos((rotationYaw / 180F) * (float)Math.PI) * MathHelper.cos((rotationPitch / 180F) * (float)Math.PI);
        motionY = -MathHelper.sin((rotationPitch / 180F) * (float)Math.PI);
        setArrowHeading(motionX, motionY, motionZ, 0.8F, 3F);
    }

    /**
     * Called to update the entity's position/logic.
     */
    public void onUpdate()
    {
        super.onUpdate();

        if (inGround || beenInGround)
        {
            return;
        }

        rotationPitch -= 70F;

        if (soundTimer >= 3)
        {
            if (!isInsideOfMaterial(Material.water))
            {
                worldObj.playSoundAtEntity(this, "random.bow", 0.6F, 1.0F / (rand.nextFloat() * 0.2F + 0.6F + (float)ticksInAir / 15F));
            }

            soundTimer = 0;
        }

        soundTimer++;
    }

    public void onEntityHit(Entity entity)
    {
        if (worldObj.isRemote)
        {
            return;
        }

        DamageSource damagesource = null;

        if (shootingEntity == null)
        {
            damagesource = DamageSourceMekanism.causeWeaponDamage(this, this);
        }
        else
        {
            damagesource = DamageSourceMekanism.causeWeaponDamage(this, shootingEntity);
        }

        if (entity.attackEntityFrom(damagesource, thrownItem.getDamageVsEntity(entity)))
        {
            if (thrownItem.getItemDamage() + 2 > thrownItem.getMaxDamage())
            {
                thrownItem.stackSize--;
                setDead();
            }
            else
            {
                thrownItem.damageItem(2, null);
                setVelocity(0.20000000000000001D * rand.nextDouble() - 0.10000000000000001D, 0.20000000000000001D * rand.nextDouble() - 0.10000000000000001D, 0.20000000000000001D * rand.nextDouble() - 0.10000000000000001D);
            }
        }
        else
        {
            bounceBack();
        }
    }

    public boolean aimRotation()
    {
        return beenInGround;
    }

    public int getMaxArrowShake()
    {
        return 4;
    }

    public float getGravity()
    {
        return 0.03F;
    }

    public ItemStack getPickupItem()
    {
        return thrownItem;
    }

    /**
     * (abstract) Protected helper method to write subclass entity data to NBT.
     */
    public void writeEntityToNBT(NBTTagCompound nbttagcompound)
    {
        super.writeEntityToNBT(nbttagcompound);

        if (thrownItem != null)
        {
            nbttagcompound.setCompoundTag("thrownItem", thrownItem.writeToNBT(new NBTTagCompound()));
        }
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    public void readEntityFromNBT(NBTTagCompound nbttagcompound)
    {
        super.readEntityFromNBT(nbttagcompound);
        thrownItem = ItemStack.loadItemStackFromNBT(nbttagcompound.getCompoundTag("thrownItem"));
    }
}
