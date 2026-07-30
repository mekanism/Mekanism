package mekanism.api.tier;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;

/// Enum representing the different tiers of alloys.
public enum AlloyTier implements ITier, IAlloyTier {
    INFUSED("infused", BaseTier.ADVANCED),
    REINFORCED("reinforced", BaseTier.ELITE),
    ATOMIC("atomic", BaseTier.ULTIMATE);

    /// @since 10.8.0
    public static final IntFunction<AlloyTier> BY_ID = ByIdMap.continuous(AlloyTier::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    /// @since 10.8.0
    public static final StreamCodec<ByteBuf, AlloyTier> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, AlloyTier::ordinal);
    /// @since 10.8.0
    public static final Codec<AlloyTier> CODEC = StringRepresentable.fromEnum(AlloyTier::values);

    private final BaseTier baseTier;
    private final String name;

    AlloyTier(String name, BaseTier base) {
        baseTier = base;
        this.name = name;
    }

    /// Gets the name of this alloy tier.
    ///
    /// @since 10.8.0, previously was `getName`
    @Override
    public String getSerializedName() {
        return name;
    }

    @Override
    public BaseTier getBaseTier() {
        return baseTier;
    }

    @Override
    public int getBaseTierLevel() {
        return ITier.super.getBaseTierLevel();
    }
}