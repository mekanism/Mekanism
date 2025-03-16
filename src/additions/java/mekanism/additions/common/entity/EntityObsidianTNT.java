package mekanism.additions.common.entity;

import mekanism.additions.common.config.MekanismAdditionsConfig;
import mekanism.additions.common.registries.AdditionsBlocks;
import mekanism.additions.common.registries.AdditionsEntityTypes;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EntityObsidianTNT extends PrimedTnt {

    public EntityObsidianTNT(EntityType<EntityObsidianTNT> type, Level world) {
        super(type, world);
        setFuse(MekanismAdditionsConfig.additions.obsidianTNTDelay.get());
        setBlockState(AdditionsBlocks.OBSIDIAN_TNT.defaultState());
    }

    @Nullable
    public static EntityObsidianTNT create(Level world, double x, double y, double z, @Nullable LivingEntity igniter) {
        EntityObsidianTNT tnt = AdditionsEntityTypes.OBSIDIAN_TNT.get().create(world);
        if (tnt == null) {
            return null;
        }
        //From TNTEntity constructor
        tnt.setPos(x, y, z);
        double d0 = world.random.nextDouble() * Mth.TWO_PI;
        tnt.setDeltaMovement(-Math.sin(d0) * 0.02D, 0.2F, -Math.cos(d0) * 0.02D);
        tnt.xo = x;
        tnt.yo = y;
        tnt.zo = z;
        tnt.owner = igniter;
        //End TNTEntity constructor
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
            level().addParticle(ParticleTypes.LAVA, getX(), getY() + 0.5, getZ(), 0, 0, 0);
        }
    }

    @Override
    protected void explode() {
        level().explode(this, Explosion.getDefaultDamageSource(level(), this), usedPortal ? USED_PORTAL_DAMAGE_CALCULATOR : null,
              getX(), getY(0.0625), getZ(), MekanismAdditionsConfig.additions.obsidianTNTBlastRadius.get(), false, ExplosionInteraction.TNT);
    }

    @NotNull
    @Override
    public EntityType<?> getType() {
        return AdditionsEntityTypes.OBSIDIAN_TNT.value();
    }

    @Override
    public ItemStack getPickedResult(HitResult target) {
        return new ItemStack(AdditionsBlocks.OBSIDIAN_TNT);
    }
}