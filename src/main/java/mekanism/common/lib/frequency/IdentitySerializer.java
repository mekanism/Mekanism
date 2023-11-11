package mekanism.common.lib.frequency;

import java.util.UUID;
import mekanism.api.NBTConstants;
import mekanism.api.security.SecurityMode;
import mekanism.common.lib.frequency.Frequency.FrequencyIdentity;
import mekanism.common.network.BasePacketHandler;
import mekanism.common.util.NBTUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

public interface IdentitySerializer {

    IdentitySerializer NAME = new IdentitySerializer() {
        @Override
        public FrequencyIdentity read(FriendlyByteBuf buf) {
            return new FrequencyIdentity(BasePacketHandler.readString(buf), buf.readEnum(SecurityMode.class), BasePacketHandler.readOptional(buf, FriendlyByteBuf::readUUID));
        }

        @Override
        public FrequencyIdentity load(CompoundTag data) {
            String name = data.getString(NBTConstants.NAME);
            if (!name.isEmpty()) {
                if (data.hasUUID(NBTConstants.OWNER_UUID)) {
                    return new FrequencyIdentity(name, SecurityMode.byIndexStatic(data.getInt(NBTConstants.SECURITY_MODE)), data.getUUID(NBTConstants.OWNER_UUID));
                } else {
                    return new FrequencyIdentity(name, SecurityMode.byIndexStatic(data.getInt(NBTConstants.SECURITY_MODE)), null);
                }
            }
            return null;
        }

        @Override
        public void write(FriendlyByteBuf buf, FrequencyIdentity data) {
            buf.writeUtf(data.key().toString());
            buf.writeEnum(data.securityMode());
            BasePacketHandler.writeOptional(buf, data.ownerUUID(), FriendlyByteBuf::writeUUID);
        }

        @Override
        public CompoundTag serialize(FrequencyIdentity data) {
            CompoundTag tag = new CompoundTag();
            tag.putString(NBTConstants.NAME, data.key().toString());
            NBTUtils.writeEnum(tag, NBTConstants.SECURITY_MODE, data.securityMode());
            if (data.ownerUUID() != null) {
                tag.putUUID(NBTConstants.OWNER_UUID, data.ownerUUID());
            }
            return tag;
        }
    };

    IdentitySerializer UUID = new IdentitySerializer() {
        @Override
        public FrequencyIdentity read(FriendlyByteBuf buf) {
            return new FrequencyIdentity(buf.readUUID(), buf.readEnum(SecurityMode.class), null);
        }

        @Override
        public FrequencyIdentity load(CompoundTag data) {
            if (data.hasUUID(NBTConstants.OWNER_UUID)) {
                return new FrequencyIdentity(data.getUUID(NBTConstants.OWNER_UUID), SecurityMode.byIndexStatic(data.getInt(NBTConstants.PUBLIC_FREQUENCY)), null);
            }
            return null;
        }

        @Override
        public void write(FriendlyByteBuf buf, FrequencyIdentity data) {
            buf.writeUUID((UUID) data.key());
            buf.writeEnum(data.securityMode());
        }

        @Override
        public CompoundTag serialize(FrequencyIdentity data) {
            CompoundTag tag = new CompoundTag();
            tag.putUUID(NBTConstants.OWNER_UUID, (UUID) data.key());
            NBTUtils.writeEnum(tag, NBTConstants.SECURITY_MODE, data.securityMode());
            return tag;
        }
    };

    FrequencyIdentity read(FriendlyByteBuf buf);

    FrequencyIdentity load(CompoundTag data);

    void write(FriendlyByteBuf buf, FrequencyIdentity data);

    CompoundTag serialize(FrequencyIdentity data);
}
