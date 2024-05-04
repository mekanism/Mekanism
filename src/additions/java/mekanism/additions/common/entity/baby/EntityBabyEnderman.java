package mekanism.additions.common.entity.baby;

import mekanism.additions.common.registries.AdditionsEntityTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.level.Level;

public class EntityBabyEnderman extends EnderMan {

    public EntityBabyEnderman(EntityType<EntityBabyEnderman> type, Level world) {
        super(type, world);
        AdditionsEntityTypes.setupBabyModifiers(this);
    }

    @Override
    public boolean isBaby() {
        return true;
    }

    @Override
    public int getExperienceReward() {
        int oldXp = xpReward;
        xpReward = (int) (xpReward * 2.5F);
        int reward = super.getExperienceReward();
        xpReward = oldXp;
        return reward;
    }
}