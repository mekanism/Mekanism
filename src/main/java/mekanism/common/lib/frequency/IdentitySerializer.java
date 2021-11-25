package mekanism.common.lib.frequency;

import java.util.UUID;
import mekanism.api.NBTConstants;
import mekanism.common.lib.frequency.Frequency.FrequencyIdentity;
import mekanism.common.network.BasePacketHandler;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;

public interface IdentitySerializer {

    IdentitySerializer NAME = new IdentitySerializer() {
        @Override
        public FrequencyIdentity read(PacketBuffer buf) {
            return new FrequencyIdentity(BasePacketHandler.readString(buf), buf.readBoolean());
        }

        @Override
        public FrequencyIdentity load(CompoundNBT data) {
            String name = data.getString(NBTConstants.NAME);
            if (!name.isEmpty()) {
                return new FrequencyIdentity(name, data.getBoolean(NBTConstants.PUBLIC_FREQUENCY));
            }
            return null;
        }

        @Override
        public void write(PacketBuffer buf, FrequencyIdentity data) {
            buf.writeUtf(data.getKey().toString());
            buf.writeBoolean(data.isPublic());
        }

        @Override
        public CompoundNBT serialize(FrequencyIdentity data) {
            CompoundNBT tag = new CompoundNBT();
            tag.putString(NBTConstants.NAME, data.getKey().toString());
            tag.putBoolean(NBTConstants.PUBLIC_FREQUENCY, data.isPublic());
            return tag;
        }
    };

    IdentitySerializer UUID = new IdentitySerializer() {
        @Override
        public FrequencyIdentity read(PacketBuffer buf) {
            return new FrequencyIdentity(buf.readUUID(), buf.readBoolean());
        }

        @Override
        public FrequencyIdentity load(CompoundNBT data) {
            if (data.hasUUID(NBTConstants.OWNER_UUID)) {
                return new FrequencyIdentity(data.getUUID(NBTConstants.OWNER_UUID), data.getBoolean(NBTConstants.PUBLIC_FREQUENCY));
            }
            return null;
        }

        @Override
        public void write(PacketBuffer buf, FrequencyIdentity data) {
            buf.writeUUID((UUID) data.getKey());
            buf.writeBoolean(data.isPublic());
        }

        @Override
        public CompoundNBT serialize(FrequencyIdentity data) {
            CompoundNBT tag = new CompoundNBT();
            tag.putUUID(NBTConstants.OWNER_UUID, (UUID) data.getKey());
            tag.putBoolean(NBTConstants.PUBLIC_FREQUENCY, data.isPublic());
            return tag;
        }
    };

    FrequencyIdentity read(PacketBuffer buf);

    FrequencyIdentity load(CompoundNBT data);

    void write(PacketBuffer buf, FrequencyIdentity data);

    CompoundNBT serialize(FrequencyIdentity data);
}
