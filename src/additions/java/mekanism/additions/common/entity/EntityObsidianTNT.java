package mekanism.additions.common.entity;

import javax.annotation.Nonnull;
import mekanism.additions.common.config.MekanismAdditionsConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MoverType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Explosion.Mode;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

public class EntityObsidianTNT extends Entity {

    /**
     * How long the fuse is
     */
    public int fuse;

    /**
     * Whether or not the TNT has exploded
     */
    private boolean hasExploded = false;

    public EntityObsidianTNT(EntityType<EntityObsidianTNT> type, World world) {
        super(type, world);
        fuse = 0;
        preventEntitySpawning = true;
    }

    public EntityObsidianTNT(World world, double x, double y, double z) {
        this(AdditionsEntityTypes.OBSIDIAN_TNT, world);
        setPosition(x, y, z);
        float randPi = (float) (Math.random() * Math.PI * 2);

        setMotion(-Math.sin(randPi) * 0.02F, 0.2, -Math.cos(randPi) * 0.02F);

        fuse = MekanismAdditionsConfig.additions.obsidianTNTDelay.get();

        prevPosX = x;
        prevPosY = y;
        prevPosZ = z;
    }

    @Override
    protected void registerData() {
    }

    @Override
    protected boolean canTriggerWalking() {
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return isAlive();
    }

    @Override
    public boolean canBePushed() {
        return true;
    }

    @Override
    public void tick() {
        prevPosX = posX;
        prevPosY = posY;
        prevPosZ = posZ;

        setMotion(getMotion().subtract(0, 0.04, 0));

        move(MoverType.SELF, getMotion());

        Vec3d motion = getMotion();
        motion = motion.mul(0.98, 0.98, 0.98);
        if (onGround) {
            motion = motion.mul(0.7, -0.5, 0.7);
        }
        setMotion(motion);

        if (fuse-- <= 0) {
            if (!world.isRemote) {
                remove();
                explode();
            } else {
                if (hasExploded) {
                    remove();
                } else {
                    world.addParticle(ParticleTypes.LAVA, posX, posY + 0.5, posZ, 0, 0, 0);
                }
            }
        } else {
            world.addParticle(ParticleTypes.LAVA, posX, posY + 0.5, posZ, 0, 0, 0);
        }
    }

    private void explode() {
        //TODO: Given obsidian tnt is stronger should it destroy instead of break
        world.createExplosion(null, posX, posY, posZ, MekanismAdditionsConfig.additions.obsidianTNTBlastRadius.get(), Mode.BREAK);
        hasExploded = true;
    }

    @Override
    protected void writeAdditional(@Nonnull CompoundNBT nbtTags) {
        nbtTags.putByte("Fuse", (byte) fuse);
    }

    @Nonnull
    @Override
    public IPacket<?> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    protected void readAdditional(@Nonnull CompoundNBT nbtTags) {
        fuse = nbtTags.getByte("Fuse");
    }
}