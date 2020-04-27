package mekanism.common.frequency;

import java.util.UUID;
import mekanism.api.NBTConstants;
import mekanism.common.frequency.Frequency.FrequencyIdentity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;

public interface IdentitySerializer {
    public static final IdentitySerializer NAME = new IdentitySerializer() {
        @Override
        public FrequencyIdentity read(PacketBuffer buf) {
            return new FrequencyIdentity(buf.readString(), buf.readBoolean());
        }
        @Override
        public FrequencyIdentity load(CompoundNBT data) {
            if (!data.getString(NBTConstants.NAME).isEmpty()) {
                return new FrequencyIdentity(data.getString(NBTConstants.NAME), data.getBoolean(NBTConstants.PUBLIC_FREQUENCY));
            }
            return null;
        }
        @Override
        public void write(PacketBuffer buf, FrequencyIdentity data) {
            buf.writeString(data.getKey().toString());
            buf.writeBoolean(data.isPublic());
        }
        @Override
        public CompoundNBT serialize(FrequencyIdentity data) {
            CompoundNBT tag = new CompoundNBT();
            tag.putString(NBTConstants.NAME, (String) data.getKey());
            tag.putBoolean(NBTConstants.PUBLIC_FREQUENCY, data.isPublic());
            return tag;
        }
    };

    public static final IdentitySerializer UUID = new IdentitySerializer() {
        @Override
        public FrequencyIdentity read(PacketBuffer buf) {
            return new FrequencyIdentity(buf.readUniqueId(), buf.readBoolean());
        }
        @Override
        public FrequencyIdentity load(CompoundNBT data) {
            if (!data.getString(NBTConstants.OWNER_UUID).isEmpty()) {
                return new FrequencyIdentity(data.getString(NBTConstants.OWNER_UUID), data.getBoolean(NBTConstants.PUBLIC_FREQUENCY));
            }
            return null;
        }
        @Override
        public void write(PacketBuffer buf, FrequencyIdentity data) {
            buf.writeUniqueId((UUID) data.getKey());
            buf.writeBoolean(data.isPublic());
        }
        @Override
        public CompoundNBT serialize(FrequencyIdentity data) {
            CompoundNBT tag = new CompoundNBT();
            tag.putUniqueId(NBTConstants.OWNER_UUID, (UUID) data.getKey());
            tag.putBoolean(NBTConstants.PUBLIC_FREQUENCY, data.isPublic());
            return tag;
        }
    };

    FrequencyIdentity read(PacketBuffer buf);
    FrequencyIdentity load(CompoundNBT data);
    void write(PacketBuffer buf, FrequencyIdentity data);
    CompoundNBT serialize(FrequencyIdentity data);
}
