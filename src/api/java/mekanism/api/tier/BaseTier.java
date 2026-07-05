package mekanism.api.tier;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.Locale;
import java.util.function.IntFunction;
import mekanism.api.SupportsColorMap;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ARGB;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jspecify.annotations.Nullable;

/// The default tiers used in Mekanism.
public enum BaseTier implements StringRepresentable, SupportsColorMap {
    BASIC("Basic", 0x5FFFB8, MapColor.COLOR_LIGHT_GREEN),
    ADVANCED("Advanced", 0xFF806A, MapColor.TERRACOTTA_PINK),
    ELITE("Elite", 0x4BF8FF, MapColor.DIAMOND),
    ULTIMATE("Ultimate", 0xF787FF, MapColor.COLOR_MAGENTA),
    CREATIVE("Creative", 0x585858, MapColor.TERRACOTTA_CYAN);

    /// Gets a tier by index, wrapping for out of bounds indices.
    ///
    /// @since 10.6.0
    public static final IntFunction<BaseTier> BY_ID = ByIdMap.continuous(BaseTier::ordinal, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
    /// Stream codec for syncing tiers by index.
    ///
    /// @since 10.6.0
    public static final StreamCodec<ByteBuf, BaseTier> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, BaseTier::ordinal);

    public static final Codec<BaseTier> CODEC = StringRepresentable.fromEnum(BaseTier::values);

    private final String name;
    private final MapColor mapColor;
    private TextColor textColor;
    private int[] rgbCode;
    private int argb;

    BaseTier(String name, int rgb, MapColor mapColor) {
        this.name = name;
        this.mapColor = mapColor;
        setColorFromAtlas(new int[]{ARGB.red(rgb), ARGB.green(rgb), ARGB.blue(rgb)});
    }

    /// Gets the name of this tier.
    public String getSimpleName() {
        return name;
    }

    /// Gets the lowercase name of this tier.
    public String getLowerName() {
        return getSimpleName().toLowerCase(Locale.ROOT);
    }

    /// Gets the map color that corresponds to this tier.
    ///
    /// @since 10.4.0
    public MapColor getMapColor() {
        return mapColor;
    }

    @Override
    public int getPackedColor() {
        return argb;
    }

    /// @apiNote Modifying the returned array will result in this color object changing the color it represents, and should not be done.
    @Override
    public int[] getRgbCode() {
        return rgbCode;
    }

    /// @since 10.4.0
    @Internal
    @Override
    public void setColorFromAtlas(int[] color) {
        this.rgbCode = color;
        this.argb = ARGB.color(rgbCode[0], rgbCode[1], rgbCode[2]);
        this.textColor = TextColor.fromRgb(argb);
    }

    @Override
    public TextColor getTextColor() {
        return this.textColor;
    }

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }

    /// Helper to lookup what base tier corresponds to the given integer value.
    ///
    /// @param tier Ordinal of the tier level to get.
    ///
    /// @return the corresponding Base Tier.
    ///
    /// @since 10.7.11
    @Nullable
    public static BaseTier getTier(int tier) {
        return switch (tier) {
            case 0 -> BASIC;
            case 1 -> ADVANCED;
            case 2 -> ELITE;
            case 3 -> ULTIMATE;
            case 4 -> CREATIVE;
            default -> null;
        };
    }
}