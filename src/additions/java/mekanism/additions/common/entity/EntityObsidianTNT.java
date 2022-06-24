package mekanism.additions.common.entity;

import mekanism.additions.common.config.MekanismAdditionsConfig;
import mekanism.additions.common.registries.AdditionsBlocks;
import mekanism.additions.common.registries.AdditionsEntityTypes;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion.BlockInteraction;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EntityObsidianTNT extends PrimedTnt {

    public EntityObsidianTNT(EntityType<EntityObsidianTNT> type, Level world) {
        super(type, world);
        setFuse(MekanismAdditionsConfig.additions.obsidianTNTDelay.get());
    }

    @Nullable
    public static EntityObsidianTNT create(Level world, double x, double y, double z, @Nullable LivingEntity igniter) {
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
        if (isAlive() && getFuse() > 0) {
            level.addParticle(ParticleTypes.LAVA, getX(), getY() + 0.5, getZ(), 0, 0, 0);
        }
    }

    @Override
    protected void explode() {
        level.explode(this, getX(), getY() + (double) (getBbHeight() / 16.0F), getZ(), MekanismAdditionsConfig.additions.obsidianTNTBlastRadius.get(), BlockInteraction.BREAK);
    }

    @NotNull
    @Override
    public EntityType<?> getType() {
        return AdditionsEntityTypes.OBSIDIAN_TNT.getEntityType();
    }

    @NotNull
    @Override
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public ItemStack getPickedResult(HitResult target) {
        return AdditionsBlocks.OBSIDIAN_TNT.getItemStack();
    }
}