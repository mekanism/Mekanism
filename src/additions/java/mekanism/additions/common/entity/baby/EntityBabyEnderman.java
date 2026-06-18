package mekanism.additions.common.entity.baby;

import mekanism.additions.common.registries.AdditionsEntityTypes;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.level.Level;

public class EntityBabyEnderman extends EnderMan {

    public EntityBabyEnderman(EntityType<EntityBabyEnderman> type, Level world) {
        super(type, world);
        this.xpReward = (int) (this.xpReward * 2.5);
        AdditionsEntityTypes.setupBabyModifiers(this);
    }

    @Override
    public boolean isBaby() {
        return true;
    }

    @Override
    public EntityDimensions getDefaultDimensions(Pose pose) {
        //Note: We already have the age scale factored into the dimensions
        return getType().getDimensions();
    }

    @Override
    public boolean is(EntityType<?> type) {
        return type == EntityTypes.ENDERMAN || super.is(type);
    }
}