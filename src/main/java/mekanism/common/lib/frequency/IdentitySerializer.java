package mekanism.common.lib.frequency;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import mekanism.api.NBTConstants;
import mekanism.api.security.SecurityMode;
import mekanism.common.lib.frequency.Frequency.FrequencyIdentity;
import mekanism.common.util.NBTUtils;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;

public interface IdentitySerializer {

    IdentitySerializer NAME = new IdentitySerializer() {
        private static final Codec<FrequencyIdentity> CODEC = RecordCodecBuilder.create(instance -> instance.group(
              ExtraCodecs.NON_EMPTY_STRING.fieldOf(NBTConstants.NAME).forGetter(identity -> identity.key().toString()),
              SecurityMode.CODEC.fieldOf(NBTConstants.SECURITY_MODE).forGetter(FrequencyIdentity::securityMode),
              UUIDUtil.CODEC.fieldOf(NBTConstants.OWNER_UUID).forGetter(FrequencyIdentity::ownerUUID)
        ).apply(instance, FrequencyIdentity::new));
        private static final StreamCodec<ByteBuf, FrequencyIdentity> STREAM_CODEC = StreamCodec.composite(
              ByteBufCodecs.STRING_UTF8, data -> data.key().toString(),
              SecurityMode.STREAM_CODEC, FrequencyIdentity::securityMode,
              UUIDUtil.STREAM_CODEC, FrequencyIdentity::ownerUUID,
              FrequencyIdentity::new
        );

        @Override
        public Codec<FrequencyIdentity> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<ByteBuf, FrequencyIdentity> streamCodec() {
            return STREAM_CODEC;
        }

        @Override
        public FrequencyIdentity load(CompoundTag data) {
            String name = data.getString(NBTConstants.NAME);
            if (!name.isEmpty()) {
                SecurityMode securityMode = SecurityMode.BY_ID.apply(data.getInt(NBTConstants.SECURITY_MODE));
                if (data.hasUUID(NBTConstants.OWNER_UUID)) {
                    return new FrequencyIdentity(name, securityMode, data.getUUID(NBTConstants.OWNER_UUID));
                }
                return new FrequencyIdentity(name, securityMode, null);
            }
            return null;
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
        private static final Codec<FrequencyIdentity> CODEC = RecordCodecBuilder.create(instance -> instance.group(
              UUIDUtil.CODEC.fieldOf(NBTConstants.OWNER_UUID).forGetter(identity -> (UUID) identity.key()),
              SecurityMode.CODEC.fieldOf(NBTConstants.SECURITY_MODE).forGetter(FrequencyIdentity::securityMode)
        ).apply(instance, (owner, mode) -> new FrequencyIdentity(owner, mode, owner)));
        private static final StreamCodec<ByteBuf, FrequencyIdentity> STREAM_CODEC = StreamCodec.composite(
              UUIDUtil.STREAM_CODEC, data -> (UUID) data.key(),
              SecurityMode.STREAM_CODEC, FrequencyIdentity::securityMode,
              (owner, security) -> new FrequencyIdentity(owner, security, owner)
        );

        @Override
        public Codec<FrequencyIdentity> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<ByteBuf, FrequencyIdentity> streamCodec() {
            return STREAM_CODEC;
        }

        @Override
        public FrequencyIdentity load(CompoundTag data) {
            if (data.hasUUID(NBTConstants.OWNER_UUID)) {
                UUID owner = data.getUUID(NBTConstants.OWNER_UUID);
                return new FrequencyIdentity(owner, SecurityMode.BY_ID.apply(data.getInt(NBTConstants.PUBLIC_FREQUENCY)), owner);
            }
            return null;
        }

        @Override
        public CompoundTag serialize(FrequencyIdentity data) {
            CompoundTag tag = new CompoundTag();
            tag.putUUID(NBTConstants.OWNER_UUID, (UUID) data.key());
            NBTUtils.writeEnum(tag, NBTConstants.SECURITY_MODE, data.securityMode());
            return tag;
        }
    };

    Codec<FrequencyIdentity> codec();

    StreamCodec<ByteBuf, FrequencyIdentity> streamCodec();

    //TODO - 1.20.5: Remove in favor of using codecs?
    FrequencyIdentity load(CompoundTag data);

    CompoundTag serialize(FrequencyIdentity data);
}
