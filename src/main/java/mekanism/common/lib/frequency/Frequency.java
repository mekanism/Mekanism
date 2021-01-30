package mekanism.common.lib.frequency;

import java.util.Objects;
import java.util.UUID;
import javax.annotation.Nullable;
import mekanism.api.NBTConstants;
import mekanism.common.network.BasePacketHandler;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;

public abstract class Frequency {

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

    public void onDeactivate(TileEntity tile) {
    }

    public void update(TileEntity tile) {
    }

    public FrequencyType<?> getType() {
        return frequencyType;
    }

    public Object getKey() {
        return name;
    }

    public boolean isPublic() {
        return publicFreq;
    }

    public Frequency setPublic(boolean isPublic) {
        publicFreq = isPublic;
        return this;
    }

    public boolean isPrivate() {
        return !publicFreq;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getName() {
        return name;
    }

    @Nullable
    public UUID getOwner() {
        return ownerUUID;
    }

    public boolean ownerMatches(UUID toCheck) {
        return Objects.equals(ownerUUID, toCheck);
    }

    public String getClientOwner() {
        return clientOwner;
    }

    public void write(CompoundNBT nbtTags) {
        nbtTags.putString(NBTConstants.NAME, name);
        if (ownerUUID != null) {
            nbtTags.putUniqueId(NBTConstants.OWNER_UUID, ownerUUID);
        }
        nbtTags.putBoolean(NBTConstants.PUBLIC_FREQUENCY, publicFreq);
    }

    protected void read(CompoundNBT nbtTags) {
        name = nbtTags.getString(NBTConstants.NAME);
        NBTUtils.setUUIDIfPresent(nbtTags, NBTConstants.OWNER_UUID, uuid -> ownerUUID = uuid);
        publicFreq = nbtTags.getBoolean(NBTConstants.PUBLIC_FREQUENCY);
    }

    public void write(PacketBuffer buffer) {
        getType().write(buffer);
        buffer.writeString(name);
        if (ownerUUID == null) {
            buffer.writeBoolean(false);
        } else {
            buffer.writeBoolean(true);
            buffer.writeUniqueId(ownerUUID);
        }
        buffer.writeString(MekanismUtils.getLastKnownUsername(ownerUUID));
        buffer.writeBoolean(publicFreq);
    }

    protected void read(PacketBuffer dataStream) {
        name = BasePacketHandler.readString(dataStream);
        if (dataStream.readBoolean()) {
            ownerUUID = dataStream.readUniqueId();
        } else {
            ownerUUID = null;
        }
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
        if (obj instanceof Frequency) {
            Frequency other = (Frequency) obj;
            return name.equals(other.name) && ownerUUID != null && ownerUUID.equals(other.ownerUUID) && publicFreq == other.publicFreq;
        }
        return false;
    }

    public FrequencyIdentity getIdentity() {
        return new FrequencyIdentity(getKey(), publicFreq);
    }

    public boolean areIdentitiesEqual(Frequency other) {
        //TODO: Decide if we want to "inline" this to not require creating new identity objects
        return getIdentity().equals(other.getIdentity());
    }

    public CompoundNBT serializeIdentity() {
        return frequencyType.getIdentitySerializer().serialize(getIdentity());
    }

    public static <FREQ extends Frequency> FREQ readFromPacket(PacketBuffer dataStream) {
        return (FREQ) FrequencyType.load(dataStream).create(dataStream);
    }

    public static class FrequencyIdentity {

        private final Object key;
        private final boolean publicFreq;

        public FrequencyIdentity(Object key, boolean publicFreq) {
            this.key = key;
            this.publicFreq = publicFreq;
        }

        public Object getKey() {
            return key;
        }

        public boolean isPublic() {
            return publicFreq;
        }

        public static FrequencyIdentity load(FrequencyType<?> type, CompoundNBT tag) {
            return type.getIdentitySerializer().load(tag);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + (publicFreq ? 1_231 : 1_237);
            result = prime * result + ((key == null) ? 0 : key.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof FrequencyIdentity && ((FrequencyIdentity) obj).key.equals(key) && ((FrequencyIdentity) obj).publicFreq == publicFreq;
        }
    }
}
