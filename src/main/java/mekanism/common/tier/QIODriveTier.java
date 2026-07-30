package mekanism.common.tier;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.Locale;
import java.util.function.IntFunction;
import mekanism.api.tier.BaseTier;
import mekanism.api.tier.ITier;
import mekanism.common.content.qio.IQIODriveCapacity;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;

//TODO - 26.2: Do we want to expose this to the config? If not we could make the metadata just be the DriveCapacity record
public enum QIODriveTier implements ITier, IQIODriveCapacity {
    BASE(BaseTier.BASIC, 16_000, 128),
    HYPER_DENSE(BaseTier.ADVANCED, 128_000, 256),
    TIME_DILATING(BaseTier.ELITE, 1_048_000, 1_024),
    SUPERMASSIVE(BaseTier.ULTIMATE, 16_000_000_000L, 8_192);

    public static final IntFunction<QIODriveTier> BY_ID = ByIdMap.continuous(QIODriveTier::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    public static final StreamCodec<ByteBuf, QIODriveTier> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, QIODriveTier::ordinal);
    public static final Codec<QIODriveTier> CODEC = StringRepresentable.fromEnum(QIODriveTier::values);

    private final String serializedName;
    private final BaseTier baseTier;
    private final long count;
    private final int types;

    QIODriveTier(BaseTier tier, long count, int types) {
        this.serializedName = name().toLowerCase(Locale.ROOT);
        this.baseTier = tier;
        this.count = count;
        this.types = types;
    }

    @Override
    public BaseTier getBaseTier() {
        return baseTier;
    }

    @Override
    public String getSerializedName() {
        return this.serializedName;
    }

    @Override
    public long count() {
        return count;
    }

    @Override
    public int types() {
        return types;
    }
}
