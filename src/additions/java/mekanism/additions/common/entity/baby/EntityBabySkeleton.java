package mekanism.additions.common.entity.baby;

import mekanism.additions.common.registries.AdditionsEntityTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LevelEvent;

public class EntityBabySkeleton extends Skeleton {

    public EntityBabySkeleton(EntityType<EntityBabySkeleton> type, Level world) {
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

    @Override
    protected void doFreezeConversion() {
        convertTo(AdditionsEntityTypes.BABY_STRAY.value(), true);
        if (!this.isSilent()) {
            level().levelEvent(null, LevelEvent.SOUND_SKELETON_TO_STRAY, this.blockPosition(), 0);
        }
    }
}