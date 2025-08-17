package mekanism.additions.common.entity;

import java.util.Optional;
import java.util.UUID;
import mekanism.additions.common.AdditionsTags;
import mekanism.additions.common.registries.AdditionsEntityTypes;
import mekanism.additions.common.registries.AdditionsItems;
import mekanism.additions.common.registries.AdditionsSounds;
import mekanism.api.SerializationConstants;
import mekanism.api.text.EnumColor;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public class EntityBalloon extends Entity implements IEntityWithComplexSpawn {

    private static final EntityDataAccessor<Byte> IS_LATCHED = SynchedEntityData.defineId(EntityBalloon.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Integer> LATCHED_X = SynchedEntityData.defineId(EntityBalloon.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> LATCHED_Y = SynchedEntityData.defineId(EntityBalloon.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> LATCHED_Z = SynchedEntityData.defineId(EntityBalloon.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> LATCHED_ID = SynchedEntityData.defineId(EntityBalloon.class, EntityDataSerializers.INT);
    public static final float OFFSET = -0.275F;

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
        EntityBalloon balloon = AdditionsEntityTypes.BALLOON.get().create(entity.level());
        if (balloon == null) {
            return null;
        }
        balloon.latchedEntity = entity;
        float height = balloon.latchedEntity.getBbHeight();
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

        if (getY() >= level().getMaxBuildHeight()) {
            pop();
            return;
        }

        if (level().isClientSide) {
            if (entityData.get(IS_LATCHED) == 1) {
                latched = new BlockPos(entityData.get(LATCHED_X), entityData.get(LATCHED_Y), entityData.get(LATCHED_Z));
            } else {
                latched = null;
            }
            if (entityData.get(IS_LATCHED) == 2) {
                latchedEntity = (LivingEntity) level().getEntity(entityData.get(LATCHED_ID));
            } else {
                latchedEntity = null;
            }
        } else {
            if (hasCachedEntity) {
                if (level() instanceof ServerLevel serverLevel) {
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
                    isLatched = 1;
                } else if (latchedEntity != null) {
                    isLatched = 2;
                } else {
                    isLatched = 0;
                }
                entityData.set(IS_LATCHED, isLatched);
                entityData.set(LATCHED_X, latched == null ? 0 : latched.getX());
                entityData.set(LATCHED_Y, latched == null ? 0 : latched.getY());
                entityData.set(LATCHED_Z, latched == null ? 0 : latched.getZ());
                entityData.set(LATCHED_ID, latchedEntity == null ? -1 : latchedEntity.getId());
            }
        }

        if (!level().isClientSide) {
            if (latched != null) {
                Optional<BlockState> blockState = WorldUtils.getBlockState(level(), latched);
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

            //Note: move may adjust the delta movement, so we need to requery it
            motion = getDeltaMovement();
            motion = motion.multiply(0.98, 0, 0.98);

            if (onGround()) {
                motion = motion.multiply(0.7, 0, 0.7);
            }
            if (motion.y() == 0) {
                motion = motion.add(0, 0.04, 0);
            }
            setDeltaMovement(motion);
        } else if (latched != null) {
            setDeltaMovement(0, 0, 0);
        } else if (latchedEntity != null && latchedEntity.getHealth() > 0) {
            if (!isFlying(latchedEntity)) {
                //If an entity is flying (creative flight), don't adjust the height they are at
                Vec3 motion = latchedEntity.getDeltaMovement();
                double targetElevation = getTargetElevation(latchedEntity);
                if (latchedEntity.getY() - Mth.EPSILON < targetElevation) {
                    //The entity is below the target height, apply vertical motion to it
                    // Note: We allow for a smaller level of precision than when comparing to if it is above the height
                    // so that we can ensure entities move to above the position for purposes of pushing them over blocks
                    latchedEntity.setDeltaMovement(motion.x(), Math.max(0.04, motion.y() * 1.015), motion.z());
                    latchedEntity.hasImpulse = true;
                } else if (latchedEntity.getY() - 0.1 > targetElevation) {
                    //The entity is above the target height, apply negative vertical motion to it
                    latchedEntity.setDeltaMovement(motion.x(), Math.min(-0.04, motion.y() * 1.015), motion.z());
                    latchedEntity.hasImpulse = true;
                } else {//The entity is at the target elevation, remove any vertical motion
                    latchedEntity.setDeltaMovement(motion.x(), 0, motion.z());
                }
            }
            setPos(latchedEntity.getX(), latchedEntity.getY() + getAddedHeight(), latchedEntity.getZ());
        }
    }

    private boolean isFlying(Entity entity) {
        return entity instanceof Player player && player.getAbilities().flying;
    }

    public double getAddedHeight() {
        return latchedEntity.getBbHeight() + 0.8;
    }

    private double getTargetElevation(LivingEntity entity) {
        BlockPos pos = BlockPos.containing(entity.position());
        BlockPos.MutableBlockPos posi = new BlockPos.MutableBlockPos(pos.getX(), pos.getY(), pos.getZ());
        CollisionContext collisionContext = CollisionContext.of(entity);
        for (; posi.getY() > 0; posi.move(Direction.DOWN)) {
            if (posi.getY() < level().getMaxBuildHeight()) {
                BlockState state = level().getBlockState(posi);
                if (!state.isAir()) {
                    double stateOffset = state.getCollisionShape(level(), posi, collisionContext).max(Axis.Y);
                    if (Double.isInfinite(stateOffset) || stateOffset == 0) {
                        //Cannot determine, skip this block and go to the next one
                        continue;
                    }
                    double floor = posi.getY() + stateOffset;
                    //Note: Add some extra height to make entities float above blocks
                    return floor + Math.min(4, Mth.ceil(entity.getBbHeight()));
                }
            }
        }
        return 0;
    }

    private void pop() {
        playSound(AdditionsSounds.POP.get(), 1, 1);
        if (!level().isClientSide) {
            Vector3f col = new Vector3f(color.getColor(0), color.getColor(1), color.getColor(2));
            DustParticleOptions redstoneParticleData = new DustParticleOptions(col, 1.0F);
            Vec3 center = getBoundingBox().getCenter();
            for (int i = 0; i < 10; i++) {
                ((ServerLevel) level()).sendParticles(redstoneParticleData, center.x() + 0.6 * random.nextFloat() - 0.3, center.y() + 0.6 * random.nextFloat() - 0.3,
                      center.z() + 0.6 * random.nextFloat() - 0.3, 1, 0, 0, 0, 0);
            }
        }
        level().gameEvent(GameEvent.ENTITY_DAMAGE, position(), GameEvent.Context.of(this));
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

    @NotNull
    @Override
    protected MovementEmission getMovementEmission() {
        return MovementEmission.NONE;
    }

    @Override
    protected void defineSynchedData(@NotNull SynchedEntityData.Builder builder) {
        builder.define(IS_LATCHED, (byte) 0);
        builder.define(LATCHED_X, 0);
        builder.define(LATCHED_Y, 0);
        builder.define(LATCHED_Z, 0);
        builder.define(LATCHED_ID, -1);
    }

    @Override
    protected void readAdditionalSaveData(@NotNull CompoundTag nbtTags) {
        NBTUtils.setEnumIfPresent(nbtTags, SerializationConstants.COLOR, EnumColor.BY_ID, color -> this.color = color);
        NBTUtils.setBlockPosIfPresent(nbtTags, SerializationConstants.LATCHED, pos -> latched = pos);
        NBTUtils.setUUIDIfPresent(nbtTags, SerializationConstants.OWNER_UUID, uuid -> {
            hasCachedEntity = true;
            cachedEntityUUID = uuid;
        });
    }

    @Override
    protected void addAdditionalSaveData(@NotNull CompoundTag nbtTags) {
        NBTUtils.writeEnum(nbtTags, SerializationConstants.COLOR, color);
        if (latched != null) {
            nbtTags.put(SerializationConstants.LATCHED, NbtUtils.writeBlockPos(latched));
        }
        if (latchedEntity != null) {
            nbtTags.putUUID(SerializationConstants.OWNER_UUID, latchedEntity.getUUID());
        }
    }

    @Override
    public boolean skipAttackInteraction(@NotNull Entity entity) {
        pop();
        if (entity instanceof ServerPlayer player) {
            CriteriaTriggers.PLAYER_KILLED_ENTITY.trigger(player, this, damageSources().playerAttack(player));
        }
        return true;
    }

    @Override
    public void writeSpawnData(RegistryFriendlyByteBuf buffer) {
        buffer.writeEnum(color);
        if (latched != null) {
            buffer.writeByte((byte) 1);
            buffer.writeBlockPos(latched);
        } else if (latchedEntity != null) {
            buffer.writeByte((byte) 2);
            buffer.writeVarInt(latchedEntity.getId());
        } else {
            buffer.writeByte((byte) 0);
        }
    }

    @Override
    public void readSpawnData(RegistryFriendlyByteBuf buffer) {
        color = buffer.readEnum(EnumColor.class);
        byte type = buffer.readByte();
        if (type == 1) {
            latched = buffer.readBlockPos();
        } else if (type == 2) {
            latchedEntity = (LivingEntity) level().getEntity(buffer.readVarInt());
        } else {
            latched = null;
        }
    }

    @Override
    public void remove(@NotNull RemovalReason reason) {
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
    public boolean isInvulnerableTo(@NotNull DamageSource source) {
        return source.is(AdditionsTags.DamageTypes.BALLOON_INVULNERABLE) || super.isInvulnerableTo(source);
    }

    @Override
    public boolean hurt(@NotNull DamageSource dmgSource, float damage) {
        if (isInvulnerableTo(dmgSource)) {
            return false;
        }
        pop();
        if (dmgSource.getEntity() instanceof ServerPlayer player) {
            CriteriaTriggers.PLAYER_KILLED_ENTITY.trigger(player, this, dmgSource);
        }
        return true;
    }

    public boolean isLatched() {
        if (level().isClientSide) {
            return entityData.get(IS_LATCHED) > 0;
        }
        return latched != null || latchedEntity != null;
    }

    public boolean isLatchedToEntity() {
        return entityData.get(IS_LATCHED) == 2 && latchedEntity != null;
    }

    @NotNull
    @Override
    protected AABB makeBoundingBox() {
        AABB boundingBox = super.makeBoundingBox();
        return boundingBox.setMinY(boundingBox.minY - OFFSET)
              .setMaxY(boundingBox.maxY - OFFSET);
    }

    @Override
    public void refreshDimensions() {
        //NO-OP don't allow size to change
    }

    @Override
    public ItemStack getPickedResult(HitResult target) {
        return AdditionsItems.BALLOONS.get(color).asStack();
    }
}