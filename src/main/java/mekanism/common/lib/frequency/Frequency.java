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

    private String name;

    @Nullable
    private UUID ownerUUID;
    private String clientOwner;

    private boolean valid = true;
    private boolean publicFreq;

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

    public void tick() {
    }

    public void onRemove() {
    }

    public void onDeactivate(BlockEntity tile) {
    }

    public void update(BlockEntity tile) {
    }

    public FrequencyType<?> getType() {
        return frequencyType;
    }

    public Object getKey() {
        return name;
    }

    @Override
    public final SecurityMode getSecurity() {
        //TODO: Eventually we may want to allow for protected frequencies, at which point instead of
        // storing a boolean publicFreq we would just store the security mode
        return isPublic() ? SecurityMode.PUBLIC : SecurityMode.PRIVATE;
    }

    public boolean isPublic() {
        return publicFreq;
    }

    public Frequency setPublic(boolean isPublic) {
        publicFreq = isPublic;
        return this;
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
        nbtTags.putBoolean(NBTConstants.PUBLIC_FREQUENCY, publicFreq);
    }

    public void write(CompoundTag nbtTags) {
        writeComponentData(nbtTags);
    }

    protected void read(CompoundTag nbtTags) {
        name = nbtTags.getString(NBTConstants.NAME);
        NBTUtils.setUUIDIfPresent(nbtTags, NBTConstants.OWNER_UUID, uuid -> ownerUUID = uuid);
        publicFreq = nbtTags.getBoolean(NBTConstants.PUBLIC_FREQUENCY);
    }

    public void write(FriendlyByteBuf buffer) {
        getType().write(buffer);
        buffer.writeUtf(name);
        BasePacketHandler.writeOptional(buffer, ownerUUID, FriendlyByteBuf::writeUUID);
        buffer.writeUtf(MekanismUtils.getLastKnownUsername(ownerUUID));
        buffer.writeBoolean(publicFreq);
    }

    protected void read(FriendlyByteBuf dataStream) {
        name = BasePacketHandler.readString(dataStream);
        ownerUUID = BasePacketHandler.readOptional(dataStream, FriendlyByteBuf::readUUID);
        clientOwner = BasePacketHandler.readString(dataStream);
        publicFreq = dataStream.readBoolean();
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
        code = 31 * code + (publicFreq ? 1 : 0);
        return code;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Frequency other && publicFreq == other.publicFreq && ownerUUID != null && name.equals(other.name) && ownerUUID.equals(other.ownerUUID);
    }

    public FrequencyIdentity getIdentity() {
        return new FrequencyIdentity(getKey(), publicFreq);
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
        return (FREQ) FrequencyType.load(dataStream).create(dataStream);
    }

    public record FrequencyIdentity(Object key, boolean isPublic) {

        public static FrequencyIdentity load(FrequencyType<?> type, CompoundTag tag) {
            return type.getIdentitySerializer().load(tag);
        }
    }
}
