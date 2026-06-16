package mekanism.additions.common.entity;

import java.util.Optional;
import mekanism.additions.common.config.MekanismAdditionsConfig;
import mekanism.additions.common.registries.AdditionsBlocks;
import mekanism.additions.common.registries.AdditionsEntityTypes;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jspecify.annotations.Nullable;

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

    @Override
    public BlockState getBlockState() {
        //Note: Theoretically this shouldn't be needed to make it render correctly as we call setBlockState in the constructor,
        // but that doesn't seem to be enough
        return AdditionsBlocks.OBSIDIAN_TNT.defaultState();
    }

    @Override
    public EntityType<?> getType() {
        return AdditionsEntityTypes.OBSIDIAN_TNT.value();
    }

    @Override
    public Holder<EntityType<?>> typeHolder() {
        return getType().builtInRegistryHolder();
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return getType().getDimensions();
    }

    @Override
    public Optional<ResourceKey<LootTable>> getLootTable() {
        return getType().getDefaultLootTable();
    }

    @Override
    public ItemStack getPickResult() {
        return new ItemStack(AdditionsBlocks.OBSIDIAN_TNT);
    }
}