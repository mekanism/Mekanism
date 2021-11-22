package mekanism.additions.common.entity.baby;

import javax.annotation.Nonnull;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.Pose;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.network.NetworkHooks;

public class EntityBabyCreeper extends CreeperEntity implements IBabyEntity {

    private static final DataParameter<Boolean> IS_CHILD = EntityDataManager.defineId(EntityBabyCreeper.class, DataSerializers.BOOLEAN);

    public EntityBabyCreeper(EntityType<EntityBabyCreeper> type, World world) {
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
    public void onSyncedDataUpdated(@Nonnull DataParameter<?> key) {
        if (IS_CHILD.equals(key)) {
            refreshDimensions();
        }
        super.onSyncedDataUpdated(key);
    }

    @Override
    protected int getExperienceReward(@Nonnull PlayerEntity player) {
        if (isBaby()) {
            xpReward = (int) (xpReward * 2.5F);
        }
        return super.getExperienceReward(player);
    }

    @Override
    public double getMyRidingOffset() {
        return isBaby() ? 0 : super.getMyRidingOffset();
    }

    @Override
    protected float getStandingEyeHeight(@Nonnull Pose pose, @Nonnull EntitySize size) {
        return isBaby() ? 0.88F : super.getStandingEyeHeight(pose, size);
    }

    /**
     * Modify vanilla's explode method to half the explosion strength of baby creepers, and charged baby creepers
     */
    @Override
    protected void explodeCreeper() {
        if (!level.isClientSide) {
            Explosion.Mode mode = ForgeEventFactory.getMobGriefingEvent(level, this) ? Explosion.Mode.DESTROY : Explosion.Mode.NONE;
            float f = isPowered() ? 1 : 0.5F;
            dead = true;
            level.explode(this, getX(), getY(), getZ(), explosionRadius * f, mode);
            remove();
            spawnLingeringCloud();
        }
    }

    @Nonnull
    @Override
    public IPacket<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}