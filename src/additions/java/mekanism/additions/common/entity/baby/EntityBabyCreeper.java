package mekanism.additions.common.entity.baby;

import mekanism.additions.common.registries.AdditionsEntityTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.Level;

public class EntityBabyCreeper extends Creeper {

    public EntityBabyCreeper(EntityType<EntityBabyCreeper> type, Level world) {
        super(type, world);
        this.xpReward = (int) (this.xpReward * 2.5);
        AdditionsEntityTypes.setupBabyModifiers(this);
    }

    @Override
    public boolean isBaby() {
        return true;
    }

    /**
     * Modify vanilla's explode method to half the explosion strength of baby creepers, and charged baby creepers
     */
    @Override
    protected void explodeCreeper() {
        if (!level().isClientSide) {
            float f = isPowered() ? 1 : 0.5F;
            dead = true;
            level().explode(this, getX(), getY(), getZ(), explosionRadius * f, Level.ExplosionInteraction.MOB);
            discard();
            spawnLingeringCloud();
        }
    }
}