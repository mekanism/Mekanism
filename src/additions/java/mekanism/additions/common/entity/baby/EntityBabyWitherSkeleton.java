package mekanism.additions.common.entity.baby;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class EntityBabyWitherSkeleton extends WitherSkeleton implements IBabyEntity {

    private static final EntityDataAccessor<Boolean> IS_CHILD = SynchedEntityData.defineId(EntityBabyWitherSkeleton.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDimensions BABY_DIMENSIONS = EntityType.WITHER_SKELETON.getDimensions().scale(0.5F).withEyeHeight(1.12F);

    public EntityBabyWitherSkeleton(EntityType<EntityBabyWitherSkeleton> type, Level world) {
        super(type, world);
        setBaby(true);
    }

    @Override
    protected void defineSynchedData(@NotNull SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(IS_CHILD, true);
    }

    @Override
    public boolean isBaby() {
        return getEntityData().get(IS_CHILD);
    }

    @Override
    public void setBaby(boolean child) {
        setChild(IS_CHILD, child);
    }

    @Override
    public void onSyncedDataUpdated(@NotNull EntityDataAccessor<?> key) {
        if (IS_CHILD.equals(key)) {
            refreshDimensions();
        }
        super.onSyncedDataUpdated(key);
    }

    @Override
    public int getExperienceReward() {
        if (isBaby()) {
            int oldXp = xpReward;
            xpReward = (int) (xpReward * 2.5F);
            int reward = super.getExperienceReward();
            xpReward = oldXp;
            return reward;
        }
        return super.getExperienceReward();
    }

    @NotNull
    @Override
    public EntityDimensions getDefaultDimensions(@NotNull Pose pose) {
        return this.isBaby() ? BABY_DIMENSIONS : super.getDefaultDimensions(pose);
    }
}