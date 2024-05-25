package mekanism.common.lib.frequency;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import java.util.UUID;
import mekanism.api.SerializationConstants;
import mekanism.api.security.SecurityMode;
import mekanism.common.lib.frequency.Frequency.FrequencyIdentity;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;

public interface IdentitySerializer {

    IdentitySerializer NAME = new IdentitySerializer() {
        private static final Codec<FrequencyIdentity> CODEC = RecordCodecBuilder.create(instance -> instance.group(
              ExtraCodecs.NON_EMPTY_STRING.fieldOf(SerializationConstants.NAME).forGetter(identity -> identity.key().toString()),
              SecurityMode.CODEC.fieldOf(SerializationConstants.SECURITY_MODE).forGetter(FrequencyIdentity::securityMode),
              UUIDUtil.CODEC.optionalFieldOf(SerializationConstants.OWNER_UUID).forGetter(identity -> Optional.ofNullable(identity.ownerUUID()))
        ).apply(instance, (key, security, owner) -> new FrequencyIdentity(key, security, owner.orElse(null))));
        private static final StreamCodec<ByteBuf, FrequencyIdentity> STREAM_CODEC = StreamCodec.composite(
              ByteBufCodecs.STRING_UTF8, data -> data.key().toString(),
              SecurityMode.STREAM_CODEC, FrequencyIdentity::securityMode,
              ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC), identity -> Optional.ofNullable(identity.ownerUUID()),
              (key, security, owner) -> new FrequencyIdentity(key, security, owner.orElse(null))
        );

        @Override
        public Codec<FrequencyIdentity> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<ByteBuf, FrequencyIdentity> streamCodec() {
            return STREAM_CODEC;
        }
    };

    IdentitySerializer UUID = new IdentitySerializer() {
        private static final Codec<FrequencyIdentity> CODEC = RecordCodecBuilder.create(instance -> instance.group(
              UUIDUtil.CODEC.fieldOf(SerializationConstants.OWNER_UUID).forGetter(identity -> (UUID) identity.key()),
              SecurityMode.CODEC.fieldOf(SerializationConstants.SECURITY_MODE).forGetter(FrequencyIdentity::securityMode)
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
    };

    Codec<FrequencyIdentity> codec();

    StreamCodec<ByteBuf, FrequencyIdentity> streamCodec();
}
