package mekanism.common.lib.frequency;

import java.util.UUID;
import mekanism.api.NBTConstants;
import mekanism.common.lib.frequency.Frequency.FrequencyIdentity;
import mekanism.common.network.BasePacketHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

public interface IdentitySerializer {

    IdentitySerializer NAME = new IdentitySerializer() {
        @Override
        public FrequencyIdentity read(FriendlyByteBuf buf) {
            return new FrequencyIdentity(BasePacketHandler.readString(buf), buf.readBoolean());
        }

        @Override
        public FrequencyIdentity load(CompoundTag data) {
            String name = data.getString(NBTConstants.NAME);
            if (!name.isEmpty()) {
                return new FrequencyIdentity(name, data.getBoolean(NBTConstants.PUBLIC_FREQUENCY));
            }
            return null;
        }

        @Override
        public void write(FriendlyByteBuf buf, FrequencyIdentity data) {
            buf.writeUtf(data.key().toString());
            buf.writeBoolean(data.isPublic());
        }

        @Override
        public CompoundTag serialize(FrequencyIdentity data) {
            CompoundTag tag = new CompoundTag();
            tag.putString(NBTConstants.NAME, data.key().toString());
            tag.putBoolean(NBTConstants.PUBLIC_FREQUENCY, data.isPublic());
            return tag;
        }
    };

    IdentitySerializer UUID = new IdentitySerializer() {
        @Override
        public FrequencyIdentity read(FriendlyByteBuf buf) {
            return new FrequencyIdentity(buf.readUUID(), buf.readBoolean());
        }

        @Override
        public FrequencyIdentity load(CompoundTag data) {
            if (data.hasUUID(NBTConstants.OWNER_UUID)) {
                return new FrequencyIdentity(data.getUUID(NBTConstants.OWNER_UUID), data.getBoolean(NBTConstants.PUBLIC_FREQUENCY));
            }
            return null;
        }

        @Override
        public void write(FriendlyByteBuf buf, FrequencyIdentity data) {
            buf.writeUUID((UUID) data.key());
            buf.writeBoolean(data.isPublic());
        }

        @Override
        public CompoundTag serialize(FrequencyIdentity data) {
            CompoundTag tag = new CompoundTag();
            tag.putUUID(NBTConstants.OWNER_UUID, (UUID) data.key());
            tag.putBoolean(NBTConstants.PUBLIC_FREQUENCY, data.isPublic());
            return tag;
        }
    };

    FrequencyIdentity read(FriendlyByteBuf buf);

    FrequencyIdentity load(CompoundTag data);

    void write(FriendlyByteBuf buf, FrequencyIdentity data);

    CompoundTag serialize(FrequencyIdentity data);
}
