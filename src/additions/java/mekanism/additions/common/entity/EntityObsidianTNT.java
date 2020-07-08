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
        preventEntitySpawning = true;
    }

    public EntityObsidianTNT(World world, double x, double y, double z, @Nullable LivingEntity igniter) {
        super(world, x, y, z, igniter);
        setFuse(MekanismAdditionsConfig.additions.obsidianTNTDelay.get());
        preventEntitySpawning = true;
    }

    @Override
    public boolean canBePushed() {
        return true;
    }

    @Override
    public void tick() {
        super.tick();
        if (isAlive() && getFuse() > 0) {
            world.addParticle(ParticleTypes.LAVA, getPosX(), getPosY() + 0.5, getPosZ(), 0, 0, 0);
        }
    }

    @Override
    protected void explode() {
        world.createExplosion(this, getPosX(), getPosY() + (double) (getHeight() / 16.0F), getPosZ(), MekanismAdditionsConfig.additions.obsidianTNTBlastRadius.get(), Mode.BREAK);
    }

    @Nonnull
    @Override
    public EntityType<?> getType() {
        return AdditionsEntityTypes.OBSIDIAN_TNT.getEntityType();
    }

    @Nonnull
    @Override
    public IPacket<?> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public ItemStack getPickedResult(RayTraceResult target) {
        return AdditionsBlocks.OBSIDIAN_TNT.getItemStack();
    }
}