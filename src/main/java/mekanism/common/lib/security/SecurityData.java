package mekanism.common.lib.security;

import io.netty.buffer.ByteBuf;
import mekanism.api.security.SecurityMode;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record SecurityData(SecurityMode mode, boolean override) {

    public static final SecurityData DUMMY = new SecurityData(SecurityMode.PUBLIC, false);
    public static final StreamCodec<ByteBuf, SecurityData> STREAM_CODEC = StreamCodec.composite(
          SecurityMode.STREAM_CODEC, SecurityData::mode,
          ByteBufCodecs.BOOL, SecurityData::override,
          SecurityData::new
    );

    public SecurityData(SecurityFrequency frequency) {
        this(frequency.getSecurity(), frequency.isOverridden());
    }
}