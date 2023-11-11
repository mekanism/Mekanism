package mekanism.common.lib.frequency;

import java.util.UUID;
import mekanism.api.NBTConstants;
import mekanism.api.security.SecurityMode;
import mekanism.common.lib.frequency.Frequency.FrequencyIdentity;
import mekanism.common.util.NBTUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

public interface IdentitySerializer {

    IdentitySerializer NAME = new IdentitySerializer() {
        @Override
        public FrequencyIdentity read(FriendlyByteBuf buf) {
            return new FrequencyIdentity(buf.readUtf(), buf.readEnum(SecurityMode.class), buf.readNullable(FriendlyByteBuf::readUUID));
        }

        @Override
        public FrequencyIdentity load(CompoundTag data) {
            String name = data.getString(NBTConstants.NAME);
            if (!name.isEmpty()) {
                SecurityMode securityMode = SecurityMode.byIndexStatic(data.getInt(NBTConstants.SECURITY_MODE));
                if (data.hasUUID(NBTConstants.OWNER_UUID)) {
                    return new FrequencyIdentity(name, securityMode, data.getUUID(NBTConstants.OWNER_UUID));
                }
                return new FrequencyIdentity(name, securityMode, null);
            }
            return null;
        }

        @Override
        public void write(FriendlyByteBuf buf, FrequencyIdentity data) {
            buf.writeUtf(data.key().toString());
            buf.writeEnum(data.securityMode());
            buf.writeNullable(data.ownerUUID(), FriendlyByteBuf::writeUUID);
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
            UUID owner = buf.readUUID();
            return new FrequencyIdentity(owner, buf.readEnum(SecurityMode.class), owner);
        }

        @Override
        public FrequencyIdentity load(CompoundTag data) {
            if (data.hasUUID(NBTConstants.OWNER_UUID)) {
                UUID owner = data.getUUID(NBTConstants.OWNER_UUID);
                return new FrequencyIdentity(owner, SecurityMode.byIndexStatic(data.getInt(NBTConstants.PUBLIC_FREQUENCY)), owner);
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
