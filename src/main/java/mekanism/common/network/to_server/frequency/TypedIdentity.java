package mekanism.common.network.to_server.frequency;

import io.netty.buffer.ByteBuf;
import mekanism.common.lib.frequency.Frequency.FrequencyIdentity;
import mekanism.common.lib.frequency.FrequencyType;
import net.minecraft.network.codec.StreamCodec;

record TypedIdentity(FrequencyType<?> type, FrequencyIdentity data) {

    public static final StreamCodec<ByteBuf, TypedIdentity> STREAM_CODEC = FrequencyType.STREAM_CODEC.dispatch(TypedIdentity::type,
          type -> type.getIdentitySerializer().streamCodec().map(value -> new TypedIdentity(type, value), TypedIdentity::data));
}