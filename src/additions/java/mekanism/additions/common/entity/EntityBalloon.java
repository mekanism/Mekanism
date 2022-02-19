package mekanism.additions.common.entity;

import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.additions.common.registries.AdditionsEntityTypes;
import mekanism.additions.common.registries.AdditionsItems;
import mekanism.additions.common.registries.AdditionsSounds;
import mekanism.api.NBTConstants;
import mekanism.api.text.EnumColor;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.Pose;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.NetworkHooks;

public class EntityBalloon extends Entity implements IEntityAdditionalSpawnData {

    private static final DataParameter<Byte> IS_LATCHED = EntityDataManager.defineId(EntityBalloon.class, DataSerializers.BYTE);
    private static final DataParameter<Integer> LATCHED_X = EntityDataManager.defineId(EntityBalloon.class, DataSerializers.INT);
    private static final DataParameter<Integer> LATCHED_Y = EntityDataManager.defineId(EntityBalloon.class, DataSerializers.INT);
    private static final DataParameter<Integer> LATCHED_Z = EntityDataManager.defineId(EntityBalloon.class, DataSerializers.INT);
    private static final DataParameter<Integer> LATCHED_ID = EntityDataManager.defineId(EntityBalloon.class, DataSerializers.INT);
    private static final double OFFSET = -0.275;

    private EnumColor color = EnumColor.DARK_BLUE;
    private BlockPos latched;
    public LivingEntity latchedEntity;
    /* server-only */
    private boolean hasCachedEntity;
    private UUID cachedEntityUUID;

    public EntityBalloon(EntityType<EntityBalloon> type, World world) {
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
    public static EntityBalloon create(World world, double x, double y, double z, EnumColor c) {
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
    public static EntityBalloon create(World world, BlockPos pos, EnumColor c) {
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
                if (level instanceof ServerWorld) {
                    Entity entity = ((ServerWorld) level).getEntity(cachedEntityUUID);
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
                if (blockState.isPresent() && blockState.get().isAir(level, latched)) {
                    latched = null;
                    entityData.set(IS_LATCHED, (byte) 0);
                }
            }
            if (latchedEntity != null && (latchedEntity.getHealth() <= 0 || !latchedEntity.isAlive() || !level.getChunkSource().isEntityTickingChunk(latchedEntity))) {
                latchedEntity = null;
                entityData.set(IS_LATCHED, (byte) 0);
            }
        }

        if (!isLatched()) {
            Vector3d motion = getDeltaMovement();
            setDeltaMovement(motion.x(), Math.min(motion.y() * 1.02F, 0.2F), motion.z());

            move(MoverType.SELF, getDeltaMovement());

            motion = getDeltaMovement();
            motion = motion.multiply(0.98, 0, 0.98);

            if (onGround) {
                motion = motion.multiply(0.7, 0, 0.7);
            }
            if (motion.y() == 0) {
                motion = new Vector3d(motion.x(), 0.04, motion.z());
            }
            setDeltaMovement(motion);
        } else if (latched != null) {
            setDeltaMovement(0, 0, 0);
        } else if (latchedEntity != null && latchedEntity.getHealth() > 0) {
            int floor = getFloor(latchedEntity);
            Vector3d motion = latchedEntity.getDeltaMovement();
            if (latchedEntity.getY() - (floor + 1) < -0.1) {
                latchedEntity.setDeltaMovement(motion.x(), Math.max(0.04, motion.y() * 1.015), motion.z());
            } else if (latchedEntity.getY() - (floor + 1) > 0.1) {
                latchedEntity.setDeltaMovement(motion.x(), Math.min(-0.04, motion.y() * 1.015), motion.z());
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
                return posi.getY() + 1 + (entity instanceof PlayerEntity ? 1 : 0);
            }
        }
        return -1;
    }

    private void pop() {
        playSound(AdditionsSounds.POP.getSoundEvent(), 1, 1);
        if (!level.isClientSide) {
            RedstoneParticleData redstoneParticleData = new RedstoneParticleData(color.getColor(0), color.getColor(1), color.getColor(2), 1.0F);
            for (int i = 0; i < 10; i++) {
                ((ServerWorld) level).sendParticles(redstoneParticleData, getX() + 0.6 * random.nextFloat() - 0.3, getY() + 0.6 * random.nextFloat() - 0.3,
                      getZ() + 0.6 * random.nextFloat() - 0.3, 1, 0, 0, 0, 0);
            }
        }
        remove();
    }

    @Override
    public boolean isPushable() {
        return latched == null;
    }

    @Override
    public boolean isPickable() {
        return isAlive();
    }

    @Override
    protected boolean isMovementNoisy() {
        return false;
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    protected void readAdditionalSaveData(@Nonnull CompoundNBT nbtTags) {
        NBTUtils.setEnumIfPresent(nbtTags, NBTConstants.COLOR, EnumColor::byIndexStatic, color -> this.color = color);
        NBTUtils.setBlockPosIfPresent(nbtTags, NBTConstants.LATCHED, pos -> latched = pos);
        NBTUtils.setUUIDIfPresent(nbtTags, NBTConstants.OWNER_UUID, uuid -> {
            hasCachedEntity = true;
            cachedEntityUUID = uuid;
        });
    }

    @Override
    protected void addAdditionalSaveData(@Nonnull CompoundNBT nbtTags) {
        nbtTags.putInt(NBTConstants.COLOR, color.ordinal());
        if (latched != null) {
            nbtTags.put(NBTConstants.LATCHED, NBTUtil.writeBlockPos(latched));
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
    public IPacket<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void writeSpawnData(PacketBuffer data) {
        data.writeDouble(getX());
        data.writeDouble(getY());
        data.writeDouble(getZ());

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
    public void readSpawnData(PacketBuffer data) {
        setPos(data.readDouble(), data.readDouble(), data.readDouble());
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
    public void remove() {
        super.remove();
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
    protected float getEyeHeight(@Nonnull Pose pose, @Nonnull EntitySize size) {
        return (float) (size.height - OFFSET);
    }

    @Nonnull
    @Override
    protected AxisAlignedBB getBoundingBoxForPose(@Nonnull Pose pose) {
        return getBoundingBox(getDimensions(pose), getX(), getY(), getZ());
    }

    @Override
    public void setPos(double x, double y, double z) {
        setPosRaw(x, y, z);
        if (isAddedToWorld() && !this.level.isClientSide && level instanceof ServerWorld) {
            ((ServerWorld) this.level).updateChunkPos(this); // Forge - Process chunk registration after moving.
        }
        setBoundingBox(getBoundingBox(getDimensions(Pose.STANDING), x, y, z));
    }

    private AxisAlignedBB getBoundingBox(EntitySize size, double x, double y, double z) {
        float f = size.width / 2F;
        double posY = y - OFFSET;
        return new AxisAlignedBB(new Vector3d(x - f, posY, z - f), new Vector3d(x + f, posY + size.height, z + f));
    }

    @Override
    public void refreshDimensions() {
        //NO-OP don't allow size to change
    }

    @Override
    public void setLocationFromBoundingbox() {
        AxisAlignedBB axisalignedbb = getBoundingBox();
        //Offset the y value upwards to match where it actually should be relative to the bounding box
        setPosRaw((axisalignedbb.minX + axisalignedbb.maxX) / 2D, axisalignedbb.minY + OFFSET, (axisalignedbb.minZ + axisalignedbb.maxZ) / 2D);
        if (isAddedToWorld() && !this.level.isClientSide && level instanceof ServerWorld) {
            ((ServerWorld) this.level).updateChunkPos(this); // Forge - Process chunk registration after moving.
        }
    }

    @Override
    public ItemStack getPickedResult(RayTraceResult target) {
        return AdditionsItems.BALLOONS.get(color).getItemStack();
    }
}