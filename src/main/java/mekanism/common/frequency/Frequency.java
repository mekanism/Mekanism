package mekanism.common.frequency;

import java.util.Set;
import java.util.UUID;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import mekanism.api.Coord4D;
import mekanism.api.NBTConstants;
import mekanism.common.network.BasePacketHandler;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;

public abstract class Frequency {

    public String name;
    public UUID ownerUUID;
    public String clientOwner;

    public boolean valid = true;

    public boolean publicFreq;

    public Set<Coord4D> activeCoords = new ObjectOpenHashSet<>();

    private final FrequencyType<?> frequencyType;

    public Frequency(FrequencyType<?> frequencyType, String name, UUID uuid) {
        this.frequencyType = frequencyType;
        this.name = name;
        ownerUUID = uuid;
    }

    public Frequency(FrequencyType<?> frequencyType, CompoundNBT nbtTags, boolean fromUpdate) {
        this.frequencyType = frequencyType;
        if (fromUpdate) {
            readFromUpdateTag(nbtTags);
        } else {
            read(nbtTags);
        }
    }

    protected Frequency(FrequencyType<?> frequencyType, PacketBuffer dataStream) {
        this.frequencyType = frequencyType;
        read(dataStream);
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

    public Coord4D getClosestCoords(Coord4D coord) {
        Coord4D closest = null;
        for (Coord4D iterCoord : activeCoords) {
            if (iterCoord.equals(coord)) {
                continue;
            }
            if (closest == null) {
                closest = iterCoord;
                continue;
            }

            if (coord.dimension != closest.dimension && coord.dimension == iterCoord.dimension) {
                closest = iterCoord;
            } else if (coord.dimension != closest.dimension || coord.dimension == iterCoord.dimension) {
                if (coord.distanceTo(closest) > coord.distanceTo(iterCoord)) {
                    closest = iterCoord;
                }
            }
        }
        return closest;
    }

    public void writeToUpdateTag(CompoundNBT updateTag) {
        updateTag.putString(NBTConstants.NAME, name);
        updateTag.putUniqueId(NBTConstants.OWNER_UUID, ownerUUID);
        updateTag.putBoolean(NBTConstants.PUBLIC_FREQUENCY, publicFreq);
    }

    public void write(CompoundNBT nbtTags) {
        writeToUpdateTag(nbtTags);
    }

    protected void readFromUpdateTag(CompoundNBT updateTag) {
        name = updateTag.getString(NBTConstants.NAME);
        NBTUtils.setUUIDIfPresent(updateTag, NBTConstants.OWNER_UUID, uuid -> ownerUUID = uuid);
        publicFreq = updateTag.getBoolean(NBTConstants.PUBLIC_FREQUENCY);
    }

    protected void read(CompoundNBT nbtTags) {
        readFromUpdateTag(nbtTags);
    }

    public void write(PacketBuffer buffer) {
        getType().write(buffer);
        buffer.writeString(name);
        buffer.writeUniqueId(ownerUUID);
        buffer.writeString(MekanismUtils.getLastKnownUsername(ownerUUID));
        buffer.writeBoolean(publicFreq);
    }

    protected void read(PacketBuffer dataStream) {
        name = BasePacketHandler.readString(dataStream);
        ownerUUID = dataStream.readUniqueId();
        clientOwner = BasePacketHandler.readString(dataStream);
        publicFreq = dataStream.readBoolean();
    }

    @Override
    public int hashCode() {
        int code = 1;
        code = 31 * code + name.hashCode();
        code = 31 * code + ownerUUID.hashCode();
        code = 31 * code + (publicFreq ? 1 : 0);
        return code;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Frequency && ((Frequency) obj).name.equals(name) && ((Frequency) obj).ownerUUID.equals(ownerUUID) && ((Frequency) obj).publicFreq == publicFreq;
    }

    public FrequencyIdentity getIdentity() {
        return new FrequencyIdentity(getKey(), publicFreq);
    }

    public CompoundNBT serializeIdentity() {
        return frequencyType.getKey().serialize(getIdentity());
    }

    /**
     * If type is unrecognized falls back to default frequency type
     */
    public static Frequency readFromPacket(PacketBuffer dataStream) {
        return FrequencyType.load(dataStream).create(dataStream);
    }

    public static class FrequencyIdentity {

        private Object key;
        private boolean publicFreq;

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
            return type.getKey().load(tag);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + (publicFreq ? 1231 : 1237);
            result = prime * result + ((key == null) ? 0 : key.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof FrequencyIdentity && ((FrequencyIdentity) obj).key.equals(key) && ((FrequencyIdentity) obj).publicFreq == publicFreq;
        }
    }
}