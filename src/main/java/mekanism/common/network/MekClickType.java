package mekanism.common.network;

import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;

public enum MekClickType {
    LEFT,
    RIGHT,
    SHIFT_LEFT;

    public static final IntFunction<MekClickType> BY_ID = ByIdMap.continuous(MekClickType::ordinal, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
    public static final StreamCodec<ByteBuf, MekClickType> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, MekClickType::ordinal);

    public static MekClickType left(boolean holdingShift) {
        return holdingShift ? SHIFT_LEFT : LEFT;
    }
}