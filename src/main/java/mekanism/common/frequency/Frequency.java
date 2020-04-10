package mekanism.common.frequency;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import mekanism.api.Coord4D;
import mekanism.api.NBTConstants;
import mekanism.common.content.entangloporter.InventoryFrequency;
import mekanism.common.network.BasePacketHandler;
import mekanism.common.security.SecurityFrequency;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;

public class Frequency {

    public static final String TELEPORTER = "Teleporter";

    public String name;
    public UUID ownerUUID;
    public String clientOwner;

    public boolean valid = true;

    public boolean publicFreq;

    public Set<Coord4D> activeCoords = new ObjectOpenHashSet<>();

    //TODO: Decide if we want to use the type in the hashcode and equals implementation
    private final FrequencyType frequencyType;

    public Frequency(String name, UUID uuid) {
        this(FrequencyType.BASE, name, uuid);
    }

    public Frequency(FrequencyType frequencyType, String name, UUID uuid) {
        this.frequencyType = frequencyType;
        this.name = name;
        ownerUUID = uuid;
    }

    public Frequency(CompoundNBT nbtTags, boolean fromUpdate) {
        this(FrequencyType.BASE, nbtTags, fromUpdate);
    }

    public Frequency(FrequencyType frequencyType, CompoundNBT nbtTags, boolean fromUpdate) {
        this.frequencyType = frequencyType;
        if (fromUpdate) {
            readFromUpdateTag(nbtTags);
        } else {
            read(nbtTags);
        }
    }

    protected Frequency(FrequencyType frequencyType, PacketBuffer dataStream) {
        this.frequencyType = frequencyType;
        read(dataStream);
    }

    public FrequencyType getFrequencyType() {
        return frequencyType;
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
        buffer.writeEnumValue(getFrequencyType());
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

    public Identity getIdentity() {
        return new Identity(name, publicFreq);
    }

    /**
     * If type is unrecognized falls back to default frequency type
     */
    public static Frequency readFromPacket(PacketBuffer dataStream) {
        FrequencyType type = dataStream.readEnumValue(FrequencyType.class);
        switch (type) {
            case INVENTORY:
                return new InventoryFrequency(dataStream);
            case SECURITY:
                return new SecurityFrequency(dataStream);
            case BASE:
            default:
                return new Frequency(type, dataStream);
        }
    }

    public static class Identity {

        public String name;
        public boolean publicFreq;

        private Identity(String name, boolean publicFreq) {
            this.name = name;
            this.publicFreq = publicFreq;
        }

        @Nullable
        public static Identity load(CompoundNBT data) {
            if (!data.getString(NBTConstants.NAME).isEmpty()) {
                return new Identity(data.getString(NBTConstants.NAME), data.getBoolean(NBTConstants.PUBLIC_FREQUENCY));
            }
            return null;
        }

        public CompoundNBT serialize() {
            CompoundNBT tag = new CompoundNBT();
            tag.putString(NBTConstants.NAME, name);
            tag.putBoolean(NBTConstants.PUBLIC_FREQUENCY, publicFreq);
            return tag;
        }
    }
}