package mekanism.additions.common.entity.baby;

import mekanism.additions.common.registries.AdditionsEntityTypes;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;

public class EntityBabySkeleton extends Skeleton implements IBabyEntity {

    private static final EntityDataAccessor<Boolean> IS_CHILD = SynchedEntityData.defineId(EntityBabySkeleton.class, EntityDataSerializers.BOOLEAN);

    public EntityBabySkeleton(EntityType<EntityBabySkeleton> type, Level world) {
        super(type, world);
        setBaby(true);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        getEntityData().define(IS_CHILD, false);
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

    @Override
    public double getMyRidingOffset() {
        return isBaby() ? 0 : super.getMyRidingOffset();
    }

    @Override
    protected float getStandingEyeHeight(@NotNull Pose pose, @NotNull EntityDimensions size) {
        return this.isBaby() ? 0.93F : super.getStandingEyeHeight(pose, size);
    }

    @NotNull
    @Override
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    protected void doFreezeConversion() {
        convertTo(AdditionsEntityTypes.BABY_STRAY.getEntityType(), true);
        if (!this.isSilent()) {
            level.levelEvent(null, LevelEvent.SOUND_SKELETON_TO_STRAY, this.blockPosition(), 0);
        }
    }
}