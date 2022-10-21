package mekanism.additions.common.entity.baby;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.monster.Stray;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;

public class EntityBabyStray extends Stray implements IBabyEntity {

    private static final EntityDataAccessor<Boolean> IS_CHILD = SynchedEntityData.defineId(EntityBabyStray.class, EntityDataSerializers.BOOLEAN);

    //Copy of stray spawn restrictions
    public static boolean spawnRestrictions(EntityType<EntityBabyStray> type, ServerLevelAccessor world, MobSpawnType reason, BlockPos pos, RandomSource random) {
        BlockPos blockpos = pos;
        do {
            blockpos = blockpos.above();
        } while (world.getBlockState(blockpos).is(Blocks.POWDER_SNOW));
        return checkMonsterSpawnRules(type, world, reason, pos, random) && (reason == MobSpawnType.SPAWNER || world.canSeeSky(blockpos.below()));
    }

    public EntityBabyStray(EntityType<EntityBabyStray> type, Level world) {
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
}