package mekanism.common.lib.frequency;

import java.util.Objects;
import java.util.UUID;
import mekanism.api.IFrequency;
import mekanism.api.NBTConstants;
import mekanism.api.security.SecurityMode;
import mekanism.common.network.BasePacketHandler;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

public abstract class Frequency implements IFrequency {

    protected boolean dirty;
    private boolean removed;
    private String name;

    @Nullable
    private UUID ownerUUID;
    private String clientOwner;

    private boolean valid = true;
    private SecurityMode securityMode = SecurityMode.PUBLIC;

    private final FrequencyType<?> frequencyType;

    /**
     * @param uuid Should only be null if we have incomplete data that we are loading
     */
    public Frequency(FrequencyType<?> frequencyType, String name, @Nullable UUID uuid) {
        this(frequencyType);
        this.name = name;
        ownerUUID = uuid;
    }

    public Frequency(FrequencyType<?> frequencyType) {
        this.frequencyType = frequencyType;
    }

    /**
     * @return {@code true} if persistent data was changed and the frequency needs to be saved.
     */
    public boolean tick() {
        return dirty;
    }

    public void onRemove() {
        removed = true;
    }

    public boolean isRemoved() {
        return removed;
    }

    /**
     * @return {@code true} if persistent data was changed by deactivating the block and the frequency needs to be saved.
     */
    public boolean onDeactivate(BlockEntity tile) {
        return false;
    }

    /**
     * @return {@code true} if persistent data was changed by updating the block and the frequency needs to be saved.
     */
    public boolean update(BlockEntity tile) {
        return false;
    }

    public FrequencyType<?> getType() {
        return frequencyType;
    }

    public Object getKey() {
        return name;
    }

    @Override
    public final SecurityMode getSecurity() {
        return securityMode;
    }

    public boolean isPublic() {
        return securityMode == SecurityMode.PUBLIC;
    }

    public Frequency setPublic(boolean isPublic) {
        if (isPublic() != isPublic) {
            securityMode = isPublic ? SecurityMode.PUBLIC : SecurityMode.PRIVATE;
            dirty = true;
        }
        return this;
    }

    public void setSecurityMode(SecurityMode securityMode) {
        if (this.securityMode != securityMode) {
            this.securityMode = securityMode;
            dirty = true;
        }
    }

    @Override
    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    @Override
    public String getName() {
        return name;
    }

    @Nullable
    @Override
    public UUID getOwner() {
        return ownerUUID;
    }

    public boolean ownerMatches(UUID toCheck) {
        return Objects.equals(ownerUUID, toCheck);
    }

    public String getClientOwner() {
        return clientOwner;
    }

    public void writeComponentData(CompoundTag nbtTags) {
        nbtTags.putString(NBTConstants.NAME, name);
        if (ownerUUID != null) {
            nbtTags.putUUID(NBTConstants.OWNER_UUID, ownerUUID);
        }
        if (frequencyType != FrequencyType.SECURITY) {
            NBTUtils.writeEnum(nbtTags, NBTConstants.SECURITY_MODE, securityMode);
        }
    }

    public void write(CompoundTag nbtTags) {
        writeComponentData(nbtTags);
    }

    protected void read(CompoundTag nbtTags) {
        name = nbtTags.getString(NBTConstants.NAME);
        NBTUtils.setUUIDIfPresent(nbtTags, NBTConstants.OWNER_UUID, uuid -> ownerUUID = uuid);
        if (frequencyType != FrequencyType.SECURITY) {
            NBTUtils.setEnumIfPresent(nbtTags, NBTConstants.SECURITY_MODE, SecurityMode::byIndexStatic, mode -> securityMode = mode);
        }
    }

    public void write(FriendlyByteBuf buffer) {
        getType().write(buffer);
        buffer.writeUtf(name);
        BasePacketHandler.writeOptional(buffer, ownerUUID, FriendlyByteBuf::writeUUID);
        buffer.writeUtf(MekanismUtils.getLastKnownUsername(ownerUUID));
        if (frequencyType != FrequencyType.SECURITY) {
            buffer.writeEnum(securityMode);
        }
    }

    protected void read(FriendlyByteBuf dataStream) {
        name = BasePacketHandler.readString(dataStream);
        ownerUUID = BasePacketHandler.readOptional(dataStream, FriendlyByteBuf::readUUID);
        clientOwner = BasePacketHandler.readString(dataStream);
        if (frequencyType != FrequencyType.SECURITY) {
            securityMode = dataStream.readEnum(SecurityMode.class);
        }
    }

    /**
     * This is the hashCode that is used for determining if a frequency is dirty. Override this if your frequency type has more things that may mean it needs to be
     * re-synced.
     */
    public int getSyncHash() {
        return hashCode();
    }

    @Override
    public int hashCode() {
        int code = 1;
        code = 31 * code + name.hashCode();
        if (ownerUUID != null) {
            code = 31 * code + ownerUUID.hashCode();
        }
        code = 31 * code + (securityMode.ordinal());
        return code;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Frequency other && securityMode == other.securityMode && ownerUUID != null && name.equals(other.name) && ownerUUID.equals(other.ownerUUID);
    }

    public FrequencyIdentity getIdentity() {
        return new FrequencyIdentity(getKey(), securityMode, ownerUUID);
    }

    public boolean areIdentitiesEqual(Frequency other) {
        //TODO: Decide if we want to "inline" this to not require creating new identity objects
        return getIdentity().equals(other.getIdentity());
    }

    public CompoundTag serializeIdentity() {
        return frequencyType.getIdentitySerializer().serialize(getIdentity());
    }

    /**
     * Like {@link #serializeIdentity()} except ensures the owner information is added if need be.
     */
    public CompoundTag serializeIdentityWithOwner() {
        CompoundTag serializedIdentity = serializeIdentity();
        if (!serializedIdentity.hasUUID(NBTConstants.OWNER_UUID) && ownerUUID != null) {
            serializedIdentity.putUUID(NBTConstants.OWNER_UUID, ownerUUID);
        }
        return serializedIdentity;
    }

    public static <FREQ extends Frequency> FREQ readFromPacket(FriendlyByteBuf dataStream) {
        return FrequencyType.<FREQ>load(dataStream).create(dataStream);
    }

    public record FrequencyIdentity(Object key, SecurityMode securityMode, @Nullable UUID ownerUUID) {
        public FrequencyIdentity(Object key, SecurityMode securityMode) {
            this(key, securityMode, null);
        }

        public static FrequencyIdentity load(FrequencyType<?> type, CompoundTag tag) {
            return type.getIdentitySerializer().load(tag);
        }
    }
}
