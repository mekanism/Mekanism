package mekanism.additions.common.entity;

import com.mojang.math.Vector3f;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.additions.common.registries.AdditionsEntityTypes;
import mekanism.additions.common.registries.AdditionsItems;
import mekanism.additions.common.registries.AdditionsSounds;
import mekanism.api.NBTConstants;
import mekanism.api.text.EnumColor;
import mekanism.common.network.BasePacketHandler;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;

public class EntityBalloon extends Entity implements IEntityAdditionalSpawnData {

    private static final EntityDataAccessor<Byte> IS_LATCHED = SynchedEntityData.defineId(EntityBalloon.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Integer> LATCHED_X = SynchedEntityData.defineId(EntityBalloon.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> LATCHED_Y = SynchedEntityData.defineId(EntityBalloon.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> LATCHED_Z = SynchedEntityData.defineId(EntityBalloon.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> LATCHED_ID = SynchedEntityData.defineId(EntityBalloon.class, EntityDataSerializers.INT);
    private static final double OFFSET = -0.275;

    private EnumColor color = EnumColor.DARK_BLUE;
    private BlockPos latched;
    public LivingEntity latchedEntity;
    /* server-only */
    private boolean hasCachedEntity;
    private UUID cachedEntityUUID;

    public EntityBalloon(EntityType<EntityBalloon> type, Level world) {
        super(type, world);

        noCulling = true;
        blocksBuilding = true;
        setPos(getX() + 0.5F, getY() + 3F, getZ() + 0.5F);
        setDeltaMovement(getDeltaMovement().x(), 0.04, getDeltaMovement().z());

        entityData.define(IS_LATCHED, (byte) 0);
        entityData.define(LATCHED_X, 0);
        entityData.define(LATCHED_Y, 0);
        entityData.define(LATCHED_Z, 0);
        entityData.define(LATCHED_ID, -1);
    }

    @Nullable
    public static EntityBalloon create(Level world, double x, double y, double z, EnumColor c) {
        EntityBalloon balloon = AdditionsEntityTypes.BALLOON.get().create(world);
        if (balloon == null) {
            return null;
        }
        balloon.setPos(x + 0.5F, y + 3F, z + 0.5F);
        balloon.xo = balloon.getX();
        balloon.yo = balloon.getY();
        balloon.zo = balloon.getZ();
        balloon.color = c;
        return balloon;
    }

    @Nullable
    public static EntityBalloon create(LivingEntity entity, EnumColor c) {
        EntityBalloon balloon = AdditionsEntityTypes.BALLOON.get().create(entity.level);
        if (balloon == null) {
            return null;
        }
        balloon.latchedEntity = entity;
        float height = balloon.latchedEntity.getDimensions(balloon.latchedEntity.getPose()).height;
        balloon.setPos(balloon.latchedEntity.getX(), balloon.latchedEntity.getY() + height + 1.7F, balloon.latchedEntity.getZ());

        balloon.xo = balloon.getX();
        balloon.yo = balloon.getY();
        balloon.zo = balloon.getZ();

        balloon.color = c;
        balloon.entityData.set(IS_LATCHED, (byte) 2);
        balloon.entityData.set(LATCHED_ID, entity.getId());
        return balloon;
    }

    @Nullable
    public static EntityBalloon create(Level world, BlockPos pos, EnumColor c) {
        EntityBalloon balloon = AdditionsEntityTypes.BALLOON.get().create(world);
        if (balloon == null) {
            return null;
        }
        balloon.latched = pos;
        balloon.setPos(balloon.latched.getX() + 0.5F, balloon.latched.getY() + 1.8F, balloon.latched.getZ() + 0.5F);

        balloon.xo = balloon.getX();
        balloon.yo = balloon.getY();
        balloon.zo = balloon.getZ();

        balloon.color = c;
        balloon.entityData.set(IS_LATCHED, (byte) 1);
        balloon.entityData.set(LATCHED_X, balloon.latched.getX());
        balloon.entityData.set(LATCHED_Y, balloon.latched.getY());
        balloon.entityData.set(LATCHED_Z, balloon.latched.getZ());
        return balloon;
    }

    public EnumColor getColor() {
        return color;
    }

    @Override
    public void tick() {
        xo = getX();
        yo = getY();
        zo = getZ();

        if (getY() >= level.getMaxBuildHeight()) {
            pop();
            return;
        }

        if (level.isClientSide) {
            if (entityData.get(IS_LATCHED) == 1) {
                latched = new BlockPos(entityData.get(LATCHED_X), entityData.get(LATCHED_Y), entityData.get(LATCHED_Z));
            } else {
                latched = null;
            }
            if (entityData.get(IS_LATCHED) == 2) {
                latchedEntity = (LivingEntity) level.getEntity(entityData.get(LATCHED_ID));
            } else {
                latchedEntity = null;
            }
        } else {
            if (hasCachedEntity) {
                if (level instanceof ServerLevel serverLevel) {
                    Entity entity = serverLevel.getEntity(cachedEntityUUID);
                    if (entity instanceof LivingEntity) {
                        latchedEntity = (LivingEntity) entity;
                    }
                }
                cachedEntityUUID = null;
                hasCachedEntity = false;
            }
            if (tickCount == 1) {
                byte isLatched;
                if (latched != null) {
                    isLatched = (byte) 1;
                } else if (latchedEntity != null) {
                    isLatched = (byte) 2;
                } else {
                    isLatched = (byte) 0;
                }
                entityData.set(IS_LATCHED, isLatched);
                entityData.set(LATCHED_X, latched == null ? 0 : latched.getX());
                entityData.set(LATCHED_Y, latched == null ? 0 : latched.getY());
                entityData.set(LATCHED_Z, latched == null ? 0 : latched.getZ());
                entityData.set(LATCHED_ID, latchedEntity == null ? -1 : latchedEntity.getId());
            }
        }

        if (!level.isClientSide) {
            if (latched != null) {
                Optional<BlockState> blockState = WorldUtils.getBlockState(level, latched);
                if (blockState.isPresent() && blockState.get().isAir()) {
                    latched = null;
                    entityData.set(IS_LATCHED, (byte) 0);
                }
            }
            if (latchedEntity != null && !latchedEntity.isAlive()) {
                latchedEntity = null;
                entityData.set(IS_LATCHED, (byte) 0);
            }
        }

        if (!isLatched()) {
            Vec3 motion = getDeltaMovement();
            setDeltaMovement(motion.x(), Math.min(motion.y() * 1.02F, 0.2F), motion.z());

            move(MoverType.SELF, getDeltaMovement());

            motion = getDeltaMovement();
            motion = motion.multiply(0.98, 0, 0.98);

            if (onGround) {
                motion = motion.multiply(0.7, 0, 0.7);
            }
            if (motion.y() == 0) {
                motion = new Vec3(motion.x(), 0.04, motion.z());
            }
            setDeltaMovement(motion);
        } else if (latched != null) {
            setDeltaMovement(0, 0, 0);
        } else if (latchedEntity != null && latchedEntity.getHealth() > 0) {
            int floor = getFloor(latchedEntity);
            Vec3 motion = latchedEntity.getDeltaMovement();
            if (latchedEntity.getY() - (floor + 1) < -0.1) {
                latchedEntity.setDeltaMovement(motion.x(), Math.max(0.04, motion.y() * 1.015), motion.z());
                latchedEntity.hasImpulse = true;
            } else if (latchedEntity.getY() - (floor + 1) > 0.1) {
                latchedEntity.setDeltaMovement(motion.x(), Math.min(-0.04, motion.y() * 1.015), motion.z());
                latchedEntity.hasImpulse = true;
            } else {
                latchedEntity.setDeltaMovement(motion.x(), 0, motion.z());
            }
            setPos(latchedEntity.getX(), latchedEntity.getY() + getAddedHeight(), latchedEntity.getZ());
        }
    }

    public double getAddedHeight() {
        return latchedEntity.getDimensions(latchedEntity.getPose()).height + 0.8;
    }

    private int getFloor(LivingEntity entity) {
        BlockPos pos = new BlockPos(entity.position());
        for (BlockPos posi = pos; posi.getY() > 0; posi = posi.below()) {
            if (posi.getY() < level.getMaxBuildHeight() && !level.isEmptyBlock(posi)) {
                return posi.getY() + 1 + (entity instanceof Player ? 1 : 0);
            }
        }
        return -1;
    }

    private void pop() {
        playSound(AdditionsSounds.POP.get(), 1, 1);
        if (!level.isClientSide) {
            Vector3f col = new Vector3f(color.getColor(0), color.getColor(1), color.getColor(2));
            DustParticleOptions redstoneParticleData = new DustParticleOptions(col, 1.0F);
            Vec3 center = getBoundingBox().getCenter();
            for (int i = 0; i < 10; i++) {
                ((ServerLevel) level).sendParticles(redstoneParticleData, center.x() + 0.6 * random.nextFloat() - 0.3, center.y() + 0.6 * random.nextFloat() - 0.3,
                      center.z() + 0.6 * random.nextFloat() - 0.3, 1, 0, 0, 0, 0);
            }
        }
        discard();
    }

    @Override
    public boolean isPushable() {
        return latched == null;
    }

    @Override
    public boolean isPickable() {
        return isAlive();
    }

    @Nonnull
    @Override
    protected MovementEmission getMovementEmission() {
        return MovementEmission.NONE;
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    protected void readAdditionalSaveData(@Nonnull CompoundTag nbtTags) {
        NBTUtils.setEnumIfPresent(nbtTags, NBTConstants.COLOR, EnumColor::byIndexStatic, color -> this.color = color);
        NBTUtils.setBlockPosIfPresent(nbtTags, NBTConstants.LATCHED, pos -> latched = pos);
        NBTUtils.setUUIDIfPresent(nbtTags, NBTConstants.OWNER_UUID, uuid -> {
            hasCachedEntity = true;
            cachedEntityUUID = uuid;
        });
    }

    @Override
    protected void addAdditionalSaveData(@Nonnull CompoundTag nbtTags) {
        NBTUtils.writeEnum(nbtTags, NBTConstants.COLOR, color);
        if (latched != null) {
            nbtTags.put(NBTConstants.LATCHED, NbtUtils.writeBlockPos(latched));
        }
        if (latchedEntity != null) {
            nbtTags.putUUID(NBTConstants.OWNER_UUID, latchedEntity.getUUID());
        }
    }

    @Override
    public boolean skipAttackInteraction(@Nonnull Entity entity) {
        pop();
        return true;
    }

    @Nonnull
    @Override
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf data) {
        BasePacketHandler.writeVector3d(data, position());
        data.writeEnum(color);
        if (latched != null) {
            data.writeByte((byte) 1);
            data.writeBlockPos(latched);
        } else if (latchedEntity != null) {
            data.writeByte((byte) 2);
            data.writeVarInt(latchedEntity.getId());
        } else {
            data.writeByte((byte) 0);
        }
    }

    @Override
    public void readSpawnData(FriendlyByteBuf data) {
        setPos(BasePacketHandler.readVector3d(data));
        color = data.readEnum(EnumColor.class);
        byte type = data.readByte();
        if (type == 1) {
            latched = data.readBlockPos();
        } else if (type == 2) {
            latchedEntity = (LivingEntity) level.getEntity(data.readVarInt());
        } else {
            latched = null;
        }
    }

    @Override
    public void remove(@Nonnull RemovalReason reason) {
        super.remove(reason);
        if (latchedEntity != null) {
            latchedEntity.hasImpulse = false;
        }
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double dist) {
        return dist <= 64;
    }

    @Override
    public boolean shouldRender(double x, double y, double z) {
        return true;
    }

    @Override
    public boolean hurt(@Nonnull DamageSource dmgSource, float damage) {
        if (isInvulnerableTo(dmgSource)) {
            return false;
        }
        markHurt();
        if (dmgSource != DamageSource.MAGIC && dmgSource != DamageSource.DROWN && dmgSource != DamageSource.FALL) {
            pop();
            return true;
        }
        return false;
    }

    public boolean isLatched() {
        if (level.isClientSide) {
            return entityData.get(IS_LATCHED) > 0;
        }
        return latched != null || latchedEntity != null;
    }

    public boolean isLatchedToEntity() {
        return entityData.get(IS_LATCHED) == 2 && latchedEntity != null;
    }

    //Adjust various bounding boxes/eye height so that the balloon gets interacted with properly
    @Override
    protected float getEyeHeight(@Nonnull Pose pose, @Nonnull EntityDimensions size) {
        return (float) (size.height - OFFSET);
    }

    @Nonnull
    @Override
    protected AABB getBoundingBoxForPose(@Nonnull Pose pose) {
        EntityDimensions size = getDimensions(pose);
        double x = getX();
        double y = getY();
        double z = getZ();
        float f = size.width / 2F;
        double posY = y - OFFSET;
        return new AABB(new Vec3(x - f, posY, z - f), new Vec3(x + f, posY + size.height, z + f));
    }

    @Nonnull
    @Override
    protected AABB makeBoundingBox() {
        return getBoundingBoxForPose(Pose.STANDING);
    }

    @Override
    public void refreshDimensions() {
        //NO-OP don't allow size to change
    }

    @Override
    public ItemStack getPickedResult(HitResult target) {
        return AdditionsItems.BALLOONS.get(color).getItemStack();
    }
}