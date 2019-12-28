package mekanism.additions.common.entity;

import java.util.UUID;
import javax.annotation.Nonnull;
import mekanism.additions.common.registries.AdditionsEntityTypes;
import mekanism.additions.common.registries.AdditionsSounds;
import mekanism.api.Coord4D;
import mekanism.api.text.EnumColor;
import mekanism.common.registration.impl.EntityTypeRegistryObject;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.NetworkHooks;

public class EntityBalloon extends Entity implements IEntityAdditionalSpawnData {

    private static final DataParameter<Byte> IS_LATCHED = EntityDataManager.createKey(EntityBalloon.class, DataSerializers.BYTE);
    private static final DataParameter<Integer> LATCHED_X = EntityDataManager.createKey(EntityBalloon.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> LATCHED_Y = EntityDataManager.createKey(EntityBalloon.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> LATCHED_Z = EntityDataManager.createKey(EntityBalloon.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> LATCHED_ID = EntityDataManager.createKey(EntityBalloon.class, DataSerializers.VARINT);
    public EnumColor color = EnumColor.DARK_BLUE;
    public Coord4D latched;
    public LivingEntity latchedEntity;
    /* server-only */
    public boolean hasCachedEntity;
    public UUID cachedEntityUUID;

    public EntityBalloon(EntityType<EntityBalloon> type, World world) {
        super(type, world);

        ignoreFrustumCheck = true;
        preventEntitySpawning = true;
        setPosition(func_226277_ct_() + 0.5F, func_226278_cu_() + 3F, func_226281_cx_() + 0.5F);
        setMotion(0, 0.04, 0);

        dataManager.register(IS_LATCHED, (byte) 0);
        dataManager.register(LATCHED_X, 0);
        dataManager.register(LATCHED_Y, 0);
        dataManager.register(LATCHED_Z, 0);
        dataManager.register(LATCHED_ID, -1);
    }

    private EntityBalloon(EntityTypeRegistryObject<EntityBalloon> type, World world) {
        this(type.getEntityType(), world);
    }

    public EntityBalloon(World world, double x, double y, double z, EnumColor c) {
        this(AdditionsEntityTypes.BALLOON, world);
        setPosition(x + 0.5F, y + 3F, z + 0.5F);

        prevPosX = func_226277_ct_();
        prevPosY = func_226278_cu_();
        prevPosZ = func_226281_cx_();
        color = c;
    }

    public EntityBalloon(LivingEntity entity, EnumColor c) {
        this(AdditionsEntityTypes.BALLOON, entity.world);
        latchedEntity = entity;
        float height = latchedEntity.getSize(latchedEntity.getPose()).height;
        setPosition(latchedEntity.func_226277_ct_(), latchedEntity.func_226277_ct_() + height + 1.7F, latchedEntity.func_226281_cx_());

        prevPosX = func_226277_ct_();
        prevPosY = func_226277_ct_();
        prevPosZ = func_226281_cx_();

        color = c;
        dataManager.set(IS_LATCHED, (byte) 2);
        dataManager.set(LATCHED_ID, entity.getEntityId());
    }

    public EntityBalloon(World world, Coord4D obj, EnumColor c) {
        this(AdditionsEntityTypes.BALLOON, world);
        latched = obj;
        setPosition(latched.x + 0.5F, latched.y + 1.9F, latched.z + 0.5F);

        prevPosX = func_226277_ct_();
        prevPosY = func_226277_ct_();
        prevPosZ = func_226281_cx_();

        color = c;
        dataManager.set(IS_LATCHED, (byte) 1);
        dataManager.set(LATCHED_X, latched != null ? latched.x : 0); /* Latched X */
        dataManager.set(LATCHED_Y, latched != null ? latched.y : 0); /* Latched Y */
        dataManager.set(LATCHED_Z, latched != null ? latched.z : 0); /* Latched Z */
    }

    @Override
    public void tick() {
        prevPosX = func_226277_ct_();
        prevPosY = func_226277_ct_();
        prevPosZ = func_226281_cx_();

        if (func_226277_ct_() > 255) {
            pop();
            return;
        }

        if (world.isRemote) {
            if (dataManager.get(IS_LATCHED) == 1) {
                latched = new Coord4D(dataManager.get(LATCHED_X), dataManager.get(LATCHED_Y), dataManager.get(LATCHED_Z), world.getDimension().getType());
            } else {
                latched = null;
            }
            if (dataManager.get(IS_LATCHED) == 2) {
                latchedEntity = (LivingEntity) world.getEntityByID(dataManager.get(LATCHED_ID));
            } else {
                latchedEntity = null;
            }
        } else {
            if (hasCachedEntity) {
                findCachedEntity();
                cachedEntityUUID = null;
                hasCachedEntity = false;
            }
            if (ticksExisted == 1) {
                byte isLatched;
                if (latched != null) {
                    isLatched = (byte) 1;
                } else if (latchedEntity != null) {
                    isLatched = (byte) 2;
                } else {
                    isLatched = (byte) 0;
                }
                dataManager.set(IS_LATCHED, isLatched);
                dataManager.set(LATCHED_X, latched != null ? latched.x : 0);
                dataManager.set(LATCHED_Y, latched != null ? latched.y : 0);
                dataManager.set(LATCHED_Z, latched != null ? latched.z : 0);
                dataManager.set(LATCHED_ID, latchedEntity != null ? latchedEntity.getEntityId() : -1);
            }
        }

        if (!world.isRemote) {
            if (latched != null) {
                BlockPos latchedPos = latched.getPos();
                if (world.isBlockLoaded(latchedPos) && world.isAirBlock(latchedPos)) {
                    latched = null;
                    dataManager.set(IS_LATCHED, (byte) 0);
                }
            }
            //TODO: Get nearby entities
            if (latchedEntity != null && (latchedEntity.getHealth() <= 0 || !latchedEntity.isAlive()/* || !world.loadedEntityList.contains(latchedEntity)*/)) {
                latchedEntity = null;
                dataManager.set(IS_LATCHED, (byte) 0);
            }
        }

        if (!isLatched()) {
            Vec3d motion = getMotion();
            setMotion(motion.getX(), Math.min(motion.getY() * 1.02F, 0.2F), motion.getZ());

            move(MoverType.SELF, getMotion());

            motion = getMotion();
            motion = motion.mul(0.98, 0, 0.98);

            if (onGround) {
                motion = motion.mul(0.7, 0, 0.7);
            }
            if (motion.getY() == 0) {
                motion = new Vec3d(motion.getX(), 0.04, motion.getZ());
            }
            setMotion(motion);
        } else if (latched != null) {
            setMotion(0, 0, 0);
        } else if (latchedEntity != null && latchedEntity.getHealth() > 0) {
            int floor = getFloor(latchedEntity);
            Vec3d motion = latchedEntity.getMotion();
            if (latchedEntity.func_226277_ct_() - (floor + 1) < -0.1) {
                latchedEntity.setMotion(motion.getX(), Math.max(0.04, motion.getY() * 1.015), motion.getZ());
            } else if (latchedEntity.func_226277_ct_() - (floor + 1) > 0.1) {
                latchedEntity.setMotion(motion.getX(), Math.min(-0.04, motion.getY() * 1.015), motion.getZ());
            } else {
                latchedEntity.setMotion(motion.getX(), 0, motion.getZ());
            }
            setPosition(latchedEntity.func_226277_ct_(), latchedEntity.func_226277_ct_() + getAddedHeight(), latchedEntity.func_226281_cx_());
        }
    }

    public double getAddedHeight() {
        return latchedEntity.getSize(latchedEntity.getPose()).height + 0.8;
    }

    private int getFloor(LivingEntity entity) {
        BlockPos pos = new BlockPos(entity);
        for (BlockPos posi = pos; posi.getY() > 0; posi = posi.down()) {
            if (posi.getY() < 256 && !world.isAirBlock(posi)) {
                return posi.getY() + 1 + (entity instanceof PlayerEntity ? 1 : 0);
            }
        }
        return -1;
    }

    private void findCachedEntity() {
        //TODO: Get nearby entities
        /*for (Object obj : world.loadedEntityList) {
            if (obj instanceof LivingEntity) {
                LivingEntity entity = (LivingEntity) obj;
                if (entity.getUniqueID().equals(cachedEntityUUID)) {
                    latchedEntity = entity;
                }
            }
        }*/
    }

    private void pop() {
        playSound(AdditionsSounds.POP.getSoundEvent(), 1, 1);
        if (world.isRemote) {
            for (int i = 0; i < 10; i++) {
                try {
                    doParticle();
                } catch (Throwable ignored) {
                }
            }
        }
        remove();
    }

    private void doParticle() {
        world.addParticle(new RedstoneParticleData(color.getColor(0), color.getColor(1), color.getColor(2), 1.0F),
              func_226277_ct_() + (rand.nextFloat() * 0.6 - 0.3), func_226278_cu_() + (rand.nextFloat() * 0.6 - 0.3), func_226281_cx_() + (rand.nextFloat() * 0.6 - 0.3), 0, 0, 0);
    }

    @Override
    public boolean canBePushed() {
        return latched == null;
    }

    @Override
    public boolean canBeCollidedWith() {
        return isAlive();
    }

    @Override
    protected boolean func_225502_at_() {
        return false;
    }

    @Override
    protected void registerData() {
    }

    @Override
    protected void readAdditional(@Nonnull CompoundNBT nbtTags) {
        color = EnumColor.values()[nbtTags.getInt("color")];
        if (nbtTags.contains("latched")) {
            latched = Coord4D.read(nbtTags.getCompound("latched"));
        }
        if (nbtTags.contains("idMost")) {
            hasCachedEntity = true;
            cachedEntityUUID = new UUID(nbtTags.getLong("idMost"), nbtTags.getLong("idLeast"));
        }
    }

    @Override
    protected void writeAdditional(@Nonnull CompoundNBT nbtTags) {
        nbtTags.putInt("color", color.ordinal());
        if (latched != null) {
            nbtTags.put("latched", latched.write(new CompoundNBT()));
        }
        if (latchedEntity != null) {
            nbtTags.putLong("idMost", latchedEntity.getUniqueID().getMostSignificantBits());
            nbtTags.putLong("idLeast", latchedEntity.getUniqueID().getLeastSignificantBits());
        }
    }

    @Override
    public boolean hitByEntity(Entity entity) {
        pop();
        return true;
    }

    @Nonnull
    @Override
    public IPacket<?> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void writeSpawnData(PacketBuffer data) {
        data.writeDouble(func_226277_ct_());
        data.writeDouble(func_226278_cu_());
        data.writeDouble(func_226281_cx_());

        data.writeEnumValue(color);
        if (latched != null) {
            data.writeByte((byte) 1);
            latched.write(data);
        } else if (latchedEntity != null) {
            data.writeByte((byte) 2);
            data.writeInt(latchedEntity.getEntityId());
        } else {
            data.writeByte((byte) 0);
        }
    }

    @Override
    public void readSpawnData(PacketBuffer data) {
        setPosition(data.readDouble(), data.readDouble(), data.readDouble());
        color = data.readEnumValue(EnumColor.class);
        byte type = data.readByte();
        if (type == 1) {
            latched = Coord4D.read(data);
        } else if (type == 2) {
            latchedEntity = (LivingEntity) world.getEntityByID(data.readInt());
        } else {
            latched = null;
        }
    }

    @Override
    public void remove() {
        super.remove();
        if (latchedEntity != null) {
            latchedEntity.isAirBorne = false;
        }
    }

    @Override
    public boolean isInRangeToRenderDist(double dist) {
        return dist <= 64;
    }

    @Override
    public boolean isInRangeToRender3d(double p_145770_1_, double p_145770_3_, double p_145770_5_) {
        return true;
    }

    @Override
    public boolean attackEntityFrom(@Nonnull DamageSource dmgSource, float damage) {
        if (isInvulnerableTo(dmgSource)) {
            return false;
        } else {
            markVelocityChanged();
            if (dmgSource != DamageSource.MAGIC && dmgSource != DamageSource.DROWN && dmgSource != DamageSource.FALL) {
                pop();
                return true;
            }
            return false;
        }
    }

    public boolean isLatched() {
        if (!world.isRemote) {
            return latched != null || latchedEntity != null;
        }
        return dataManager.get(IS_LATCHED) > 0;
    }

    public boolean isLatchedToEntity() {
        return dataManager.get(IS_LATCHED) == 2 && latchedEntity != null;
    }
}