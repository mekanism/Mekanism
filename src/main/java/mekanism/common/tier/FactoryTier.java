package mekanism.common.tier;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.Locale;
import java.util.function.IntFunction;
import mekanism.api.tier.BaseTier;
import mekanism.api.tier.ITier;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;

public enum FactoryTier implements ITier {
    BASIC(BaseTier.BASIC, 3),
    ADVANCED(BaseTier.ADVANCED, 5),
    ELITE(BaseTier.ELITE, 7),
    ULTIMATE(BaseTier.ULTIMATE, 9);

    public static final IntFunction<FactoryTier> BY_ID = ByIdMap.continuous(FactoryTier::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    public static final StreamCodec<ByteBuf, FactoryTier> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, FactoryTier::ordinal);
    public static final Codec<FactoryTier> CODEC = StringRepresentable.fromEnum(FactoryTier::values);

    private final String serializedName;
    public final int processes;
    private final BaseTier baseTier;

    FactoryTier(BaseTier tier, int process) {
        this.serializedName = name().toLowerCase(Locale.ROOT);
        processes = process;
        baseTier = tier;
    }

    @Override
    public BaseTier getBaseTier() {
        return baseTier;
    }

    @Override
    public String getSerializedName() {
        return this.serializedName;
    }
}