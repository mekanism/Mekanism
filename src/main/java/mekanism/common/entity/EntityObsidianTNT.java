package mekanism.common.entity;

import javax.annotation.Nonnull;
import mekanism.common.config.MekanismConfig.general;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;

public class EntityObsidianTNT extends Entity {

    /**
     * How long the fuse is
     */
    public int fuse;

    /**
     * Whether or not the TNT has exploded
     */
    private boolean hasExploded = false;

    public EntityObsidianTNT(World world) {
        super(world);
        fuse = 0;
        preventEntitySpawning = true;
        setSize(0.98F, 0.98F);
    }

    public EntityObsidianTNT(World world, double x, double y, double z) {
        this(world);

        setPosition(x, y, z);

        float randPi = (float) (Math.random() * Math.PI * 2);

        motionX = -(Math.sin(randPi)) * 0.02F;
        motionY = 0.2;
        motionZ = -(Math.cos(randPi)) * 0.02F;

        fuse = general.obsidianTNTDelay;

        prevPosX = x;
        prevPosY = y;
        prevPosZ = z;
    }

    @Override
    protected void entityInit() {
    }

    @Override
    protected boolean canTriggerWalking() {
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return !isDead;
    }

    @Override
    public boolean canBePushed() {
        return true;
    }

    @Override
    public void onUpdate() {
        prevPosX = posX;
        prevPosY = posY;
        prevPosZ = posZ;

        motionY -= 0.04;

        move(MoverType.SELF, motionX, motionY, motionZ);

        motionX *= 0.98;
        motionY *= 0.98;
        motionZ *= 0.98;

        if (onGround) {
            motionX *= 0.7;
            motionZ *= 0.7;
            motionY *= -0.5;
        }

        if (fuse-- <= 0) {
            if (!world.isRemote) {
                setDead();
                explode();
            } else {
                if (hasExploded) {
                    setDead();
                } else {
                    world.spawnParticle(EnumParticleTypes.LAVA, posX, posY + 0.5, posZ, 0, 0, 0);
                }
            }
        } else {
            world.spawnParticle(EnumParticleTypes.LAVA, posX, posY + 0.5, posZ, 0, 0, 0);
        }
    }

    private void explode() {
        world.createExplosion(null, posX, posY, posZ, general.obsidianTNTBlastRadius, true);
        hasExploded = true;
    }

    @Override
    protected void writeEntityToNBT(@Nonnull NBTTagCompound nbtTags) {
        nbtTags.setByte("Fuse", (byte) fuse);
    }

    @Override
    protected void readEntityFromNBT(@Nonnull NBTTagCompound nbtTags) {
        fuse = nbtTags.getByte("Fuse");
    }
}
