package mekanism.additions.common.entity;

import java.util.Optional;
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
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;
import org.jspecify.annotations.Nullable;

public class EntityBalloon extends Entity implements IEntityWithComplexSpawn {

    private static final EntityDataAccessor<Optional<BlockPos>> LATCHED_POS = SynchedEntityData.defineId(EntityBalloon.class, EntityDataSerializers.OPTIONAL_BLOCK_POS);
    private static final EntityDataAccessor<Optional<EntityReference<LivingEntity>>> LATCHED_ENTITY = SynchedEntityData.defineId(EntityBalloon.class, EntityDataSerializers.OPTIONAL_LIVING_ENTITY_REFERENCE);
    public static final float OFFSET = -0.275F;

    private EnumColor color = EnumColor.DARK_BLUE;

    public EntityBalloon(EntityType<EntityBalloon> type, Level world) {
        super(type, world);
        blocksBuilding = true;
        setPos(getX() + 0.5F, getY() + 3F, getZ() + 0.5F);
        setDeltaMovement(getDeltaMovement().x(), 0.04, getDeltaMovement().z());
    }

    @Nullable
    public static EntityBalloon create(Level world, double x, double y, double z, EnumColor c) {
        EntityBalloon balloon = AdditionsEntityTypes.BALLOON.get().create(world, EntitySpawnReason.EVENT);
        if (balloon == null) {
            return null;
        }
        balloon.absSnapTo(x + 0.5F, y + 3F, z + 0.5F);
        balloon.color = c;
        return balloon;
    }

    @Nullable
    public static EntityBalloon create(LivingEntity entity, EnumColor c) {
        EntityBalloon balloon = AdditionsEntityTypes.BALLOON.get().create(entity.level(), EntitySpawnReason.EVENT);
        if (balloon == null) {
            return null;
        }
        float height = entity.getBbHeight();
        balloon.absSnapTo(entity.getX(), entity.getY() + height + 1.7F, entity.getZ());

        balloon.color = c;
        balloon.entityData.set(LATCHED_ENTITY, Optional.of(EntityReference.of(entity)));
        return balloon;
    }

    @Nullable
    public static EntityBalloon create(Level world, BlockPos pos, EnumColor c) {
        EntityBalloon balloon = AdditionsEntityTypes.BALLOON.get().create(world, EntitySpawnReason.EVENT);
        if (balloon == null) {
            return null;
        }
        balloon.absSnapTo(pos.getX() + 0.5F, pos.getY() + 1.8F, pos.getZ() + 0.5F);
        balloon.color = c;
        balloon.entityData.set(LATCHED_POS, Optional.of(pos.immutable()));
        return balloon;
    }

    public EnumColor getColor() {
        return color;
    }

    @Override
    public void tick() {
        setOldPos();

        //TODO - 26.1: Re-evaluate all these cases where we have getMaxY() + 1, to make sure the logic makes sense having the +1
        if (getY() >= level().getMaxY() + 1) {
            pop();
            return;
        }

        LivingEntity latchedEntity = latchedEntity();

        if (!level().isClientSide()) {
            BlockPos latchedPos = latchedPos();
            if (latchedPos != null) {
                Optional<BlockState> blockState = WorldUtils.getBlockState(level(), latchedPos);
                if (blockState.isPresent() && blockState.get().isAir()) {
                    //If the block this balloon was attached to is no longer present, mark the balloon as not being latched to a block
                    entityData.set(LATCHED_POS, Optional.empty());
                }
            }
            if (latchedEntity != null && !latchedEntity.isAlive()) {
                latchedEntity = null;
                entityData.set(LATCHED_ENTITY, Optional.empty());
            }
        }

        if (isLatchedToPos()) {
            setDeltaMovement(0, 0, 0);
        } else if (latchedEntity != null && latchedEntity.isAlive()) {
            if (!isFlying(latchedEntity)) {
                //If an entity is flying (creative flight), don't adjust the height they are at
                Vec3 motion = latchedEntity.getDeltaMovement();
                double targetElevation = getTargetElevation(latchedEntity);
                if (latchedEntity.getY() - Mth.EPSILON < targetElevation) {
                    //The entity is below the target height, apply vertical motion to it
                    // Note: We allow for a smaller level of precision than when comparing to if it is above the height
                    // so that we can ensure entities move to above the position for purposes of pushing them over blocks
                    latchedEntity.setDeltaMovement(motion.x(), Math.max(0.04, motion.y() * 1.015), motion.z());
                    latchedEntity.needsSync = true;
                } else if (latchedEntity.getY() - 0.1 > targetElevation) {
                    //The entity is above the target height, apply negative vertical motion to it
                    latchedEntity.setDeltaMovement(motion.x(), Math.min(-0.04, motion.y() * 1.015), motion.z());
                    latchedEntity.needsSync = true;
                } else {//The entity is at the target elevation, remove any vertical motion
                    latchedEntity.setDeltaMovement(motion.x(), 0, motion.z());
                }
            }
            setPos(latchedEntity.getX(), latchedEntity.getY() + getAddedHeight(latchedEntity), latchedEntity.getZ());
        } else {
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
        }
    }

    private boolean isFlying(Entity entity) {
        return entity instanceof Player player && player.getAbilities().flying;
    }

    public double getAddedHeight(LivingEntity latchedEntity) {
        return latchedEntity.getBbHeight() + 0.8;
    }

    private double getTargetElevation(LivingEntity entity) {
        BlockPos pos = BlockPos.containing(entity.position());
        BlockPos.MutableBlockPos posi = new BlockPos.MutableBlockPos(pos.getX(), pos.getY(), pos.getZ());
        CollisionContext collisionContext = CollisionContext.of(entity);
        for (; posi.getY() > 0; posi.move(Direction.DOWN)) {
            if (posi.getY() < level().getMaxY() + 1) {
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
        if (!level().isClientSide()) {
            DustParticleOptions redstoneParticleData = new DustParticleOptions(color.getPackedColor(), 1.0F);
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
        return !isLatchedToPos();
    }

    @Override
    public boolean isPickable() {
        return isAlive();
    }

    @Override
    protected MovementEmission getMovementEmission() {
        return MovementEmission.NONE;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(LATCHED_POS, Optional.empty());
        builder.define(LATCHED_ENTITY, Optional.empty());
    }

    @Override
    public void readAdditionalSaveData(ValueInput input) {
        NBTUtils.setEnumIfPresent(input, SerializationConstants.COLOR, EnumColor.BY_ID, color -> this.color = color);
        entityData.set(LATCHED_POS, input.read(SerializationConstants.LATCHED, BlockPos.CODEC));
        entityData.set(LATCHED_ENTITY, input.read(SerializationConstants.LATCHED_ENTITY, EntityReference.codec()));
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        NBTUtils.writeEnum(output, SerializationConstants.COLOR, color);
        output.storeNullable(SerializationConstants.LATCHED, BlockPos.CODEC, latchedPos());
        output.storeNullable(SerializationConstants.LATCHED_ENTITY, EntityReference.codec(), entityData.get(LATCHED_ENTITY).orElse(null));
    }

    @Override
    public boolean skipAttackInteraction(Entity entity) {
        pop();
        if (entity instanceof ServerPlayer player) {
            CriteriaTriggers.PLAYER_KILLED_ENTITY.trigger(player, this, damageSources().playerAttack(player));
        }
        return true;
    }

    @Override
    public void writeSpawnData(RegistryFriendlyByteBuf buffer) {
        buffer.writeEnum(color);
    }

    @Override
    public void readSpawnData(RegistryFriendlyByteBuf buffer) {
        color = buffer.readEnum(EnumColor.class);
    }

    @Override
    public void remove(RemovalReason reason) {
        super.remove(reason);
        LivingEntity latchedEntity = latchedEntity();
        if (latchedEntity != null) {
            latchedEntity.needsSync = false;
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

    public boolean isInvulnerableTo(DamageSource source) {
        return source.is(AdditionsTags.DamageTypes.BALLOON_INVULNERABLE) || super.isInvulnerableToBase(source);
    }

    @Override
    public boolean hurtClient(DamageSource source) {
        return !isInvulnerableTo(source);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource dmgSource, float damage) {
        if (isInvulnerableTo(dmgSource)) {
            return false;
        }
        pop();
        if (dmgSource.getEntity() instanceof ServerPlayer player) {
            CriteriaTriggers.PLAYER_KILLED_ENTITY.trigger(player, this, dmgSource);
        }
        return true;
    }

    @Nullable
    private BlockPos latchedPos() {
        return entityData.get(LATCHED_POS).orElse(null);
    }

    public boolean isLatchedToPos() {
        return entityData.get(LATCHED_POS).isPresent();
    }

    @Nullable
    public LivingEntity latchedEntity() {
        Optional<EntityReference<LivingEntity>> reference = entityData.get(LATCHED_ENTITY);
        //noinspection OptionalIsPresent - capturing lambda
        if (reference.isPresent()) {
            return reference.get().getEntity(level(), LivingEntity.class);
        }
        return null;
    }

    public boolean isLatchedTo(LivingEntity entity) {
        Optional<EntityReference<LivingEntity>> reference = entityData.get(LATCHED_ENTITY);
        //noinspection OptionalIsPresent - capturing lambda
        if (reference.isPresent()) {
            return reference.get().matches(entity);
        }
        return false;
    }

    @Override
    protected AABB makeBoundingBox(Vec3 position) {
        AABB boundingBox = super.makeBoundingBox(position);
        return boundingBox.setMinY(boundingBox.minY - OFFSET)
              .setMaxY(boundingBox.maxY - OFFSET);
    }

    @Override
    public void refreshDimensions() {
        //NO-OP don't allow size to change
    }

    @Override
    public ItemStack getPickResult() {
        return AdditionsItems.BALLOONS.get(color).asStack();
    }
}