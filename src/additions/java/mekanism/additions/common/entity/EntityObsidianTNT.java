package mekanism.additions.common.entity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.additions.common.config.MekanismAdditionsConfig;
import mekanism.additions.common.registries.AdditionsBlocks;
import mekanism.additions.common.registries.AdditionsEntityTypes;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.TNTEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.IPacket;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.Explosion.Mode;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

public class EntityObsidianTNT extends TNTEntity {

    public EntityObsidianTNT(EntityType<EntityObsidianTNT> type, World world) {
        super(type, world);
        setFuse(MekanismAdditionsConfig.additions.obsidianTNTDelay.get());
    }

    @Nullable
    public static EntityObsidianTNT create(World world, double x, double y, double z, @Nullable LivingEntity igniter) {
        EntityObsidianTNT tnt = AdditionsEntityTypes.OBSIDIAN_TNT.get().create(world);
        if (tnt == null) {
            return null;
        }
        //From TNTEntity constructor
        tnt.setPos(x, y, z);
        double d0 = world.random.nextDouble() * (double) ((float) Math.PI * 2F);
        tnt.setDeltaMovement(-Math.sin(d0) * 0.02D, 0.2F, -Math.cos(d0) * 0.02D);
        tnt.xo = x;
        tnt.yo = y;
        tnt.zo = z;
        tnt.owner = igniter;
        //End TNTEntity constructor
        tnt.setFuse(MekanismAdditionsConfig.additions.obsidianTNTDelay.get());
        return tnt;
    }

    @Override
    public boolean isPushable() {
        return true;
    }

    @Override
    public void tick() {
        super.tick();
        if (isAlive() && getLife() > 0) {
            level.addParticle(ParticleTypes.LAVA, getX(), getY() + 0.5, getZ(), 0, 0, 0);
        }
    }

    @Override
    protected void explode() {
        level.explode(this, getX(), getY() + (double) (getBbHeight() / 16.0F), getZ(), MekanismAdditionsConfig.additions.obsidianTNTBlastRadius.get(), Mode.BREAK);
    }

    @Nonnull
    @Override
    public EntityType<?> getType() {
        return AdditionsEntityTypes.OBSIDIAN_TNT.getEntityType();
    }

    @Nonnull
    @Override
    public IPacket<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public ItemStack getPickedResult(RayTraceResult target) {
        return AdditionsBlocks.OBSIDIAN_TNT.getItemStack();
    }
}