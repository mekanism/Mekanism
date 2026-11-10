package mekanism.additions.common.entity;

import mekanism.additions.common.config.MekanismAdditionsConfig;
import mekanism.additions.common.registries.AdditionsBlocks;
import mekanism.additions.common.registries.AdditionsEntityTypes;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
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
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        //If any of the values were missing and set to the default, instead set them to our default
        if (input.read("block_state", BlockState.CODEC).isEmpty()) {
            setBlockState(AdditionsBlocks.OBSIDIAN_TNT.defaultState());
        }
        if (input.getShortOr("fuse", (short) -2) == -2) {
            setFuse(MekanismAdditionsConfig.additions.obsidianTNTDelay.get());
        }
        if (input.getFloatOr("explosion_power", -1F) < 0) {
            this.explosionPower = MekanismAdditionsConfig.additions.obsidianTNTBlastRadius.get();
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (isAlive() && getFuse() > 0) {
            level().addParticle(ParticleTypes.LAVA, getX(), getY() + 0.5, getZ(), 0, 0, 0);
        }
    }

    @Override
    public EntityType<?> getType() {
        return AdditionsEntityTypes.OBSIDIAN_TNT.value();
    }

    @Override
    public ItemStack getPickResult() {
        return new ItemStack(AdditionsBlocks.OBSIDIAN_TNT);
    }
}