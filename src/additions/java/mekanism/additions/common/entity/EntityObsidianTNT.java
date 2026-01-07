package mekanism.additions.common.entity;

import mekanism.additions.common.config.MekanismAdditionsConfig;
import mekanism.additions.common.registries.AdditionsBlocks;
import mekanism.additions.common.registries.AdditionsEntityTypes;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EntityObsidianTNT extends PrimedTnt {

    public EntityObsidianTNT(EntityType<EntityObsidianTNT> type, Level world) {
        super(type, world);
        setData();
    }

    public EntityObsidianTNT(Level level, double x, double y, double z, @Nullable LivingEntity owner) {
        super(level, x, y, z, owner);
        setData();
    }

    private void setData() {
        setFuse(MekanismAdditionsConfig.additions.obsidianTNTDelay.get());
        setBlockState(AdditionsBlocks.OBSIDIAN_TNT.defaultState());
        explosionPower = MekanismAdditionsConfig.additions.obsidianTNTBlastRadius.get();
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

    @NotNull
    @Override
    public EntityType<?> getType() {
        return AdditionsEntityTypes.OBSIDIAN_TNT.value();
    }

    @Override
    public ItemStack getPickResult() {
        return new ItemStack(AdditionsBlocks.OBSIDIAN_TNT);
    }
}