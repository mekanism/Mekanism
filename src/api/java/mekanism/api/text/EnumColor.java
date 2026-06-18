package mekanism.api.text;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import java.util.function.IntFunction;
import mekanism.api.IIncrementalEnum;
import mekanism.api.MekanismAPI;
import mekanism.api.SupportsColorMap;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.Util;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jspecify.annotations.Nullable;

/// Simple color enum for adding colors to in-game GUI strings of text.
public enum EnumColor implements IIncrementalEnum<EnumColor>, SupportsColorMap, StringRepresentable, IHasTranslationKey {
    BLACK(DyeColor.BLACK, "Black", 0x404040),
    DARK_BLUE(DyeColor.BLUE, "Blue", 0x366BD0),
    DARK_GREEN(DyeColor.GREEN, "Green", 0x59C15F),
    DARK_AQUA(DyeColor.CYAN, "Cyan", 0x00F3D0),
    DARK_RED("dark_red", "Dark Red", 0xC9071F, MapColor.NETHER),
    PURPLE(DyeColor.PURPLE, "Purple", 0xA460D9),
    ORANGE(DyeColor.ORANGE, "Orange", 0xFFA160),
    GRAY(DyeColor.LIGHT_GRAY, "Light Gray", 0xCFCFCF),
    DARK_GRAY(DyeColor.GRAY, "Gray", 0x7A7A7A),
    INDIGO(DyeColor.LIGHT_BLUE, "Light Blue", 0x559EFF),
    BRIGHT_GREEN(DyeColor.LIME, "Lime", 0x75FF89),
    AQUA("aqua", "Aqua", 0x30FFF9, MapColor.COLOR_LIGHT_BLUE),
    RED(DyeColor.RED, "Red", 0xFF383C),
    PINK(DyeColor.MAGENTA, "Magenta", 0xD55ECB),
    YELLOW(DyeColor.YELLOW, "Yellow", 0xFFDD4F),
    WHITE(DyeColor.WHITE, "White", 0xFFFFFF),
    //Extras for dye-completeness
    BROWN(DyeColor.BROWN, "Brown", 0xA17649),
    BRIGHT_PINK(DyeColor.PINK, "Pink", 0xFFBCC4);

    /// Codec for serializing colors based on their name.
    ///
    /// @since 10.6.0
    public static final Codec<EnumColor> CODEC = StringRepresentable.fromEnum(EnumColor::values);
    /// Gets a color by index, wrapping for out of bounds indices.
    ///
    /// @since 10.6.0
    public static final IntFunction<EnumColor> BY_ID = ByIdMap.continuous(EnumColor::ordinal, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
    /// Stream codec for syncing colors by index.
    ///
    /// @since 10.6.0
    public static final StreamCodec<ByteBuf, EnumColor> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, EnumColor::ordinal);
    /// Stream codec for syncing optional colors by index.
    ///
    /// @since 10.6.0
    public static final StreamCodec<ByteBuf, Optional<EnumColor>> OPTIONAL_STREAM_CODEC = ByteBufCodecs.optional(STREAM_CODEC);
    private final String translationKey;
    private final String englishName;
    private final String registryPrefix;
    @Nullable
    private final DyeColor dyeColor;
    private final MapColor mapColor;

    private TextColor color;
    private int[] rgbCode;
    private int argb;

    EnumColor(DyeColor dyeColor, String englishName, int rgb) {
        this(dyeColor.getName(), englishName, rgb, dyeColor.getMapColor(), dyeColor);
    }

    EnumColor(String registryPrefix, String englishName, int rgb, MapColor mapColor) {
        this(registryPrefix, englishName, rgb, mapColor, null);
    }

    EnumColor(String registryPrefix, String englishName, int rgb, MapColor mapColor, @Nullable DyeColor dyeColor) {
        this.englishName = englishName;
        this.dyeColor = dyeColor;
        this.registryPrefix = registryPrefix;
        this.translationKey = Util.makeDescriptionId("color", Identifier.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, this.registryPrefix));
        this.mapColor = mapColor;
        setColorFromAtlas(new int[]{ARGB.red(rgb), ARGB.green(rgb), ARGB.blue(rgb)});
    }

    /// Gets the prefix to use in registry names for this color.
    public String getRegistryPrefix() {
        return registryPrefix;
    }

    /// Gets the English name of this color.
    public String getEnglishName() {
        return englishName;
    }

    /// Gets the material or map color that most closely corresponds to this color.
    public MapColor getMapColor() {
        return mapColor;
    }

    /// Gets the corresponding dye color or `null` if there isn't one.
    @Nullable
    public DyeColor getDyeColor() {
        return dyeColor;
    }

    /// Gets the name of this color with its color prefix code.
    ///
    /// @return the color's name and color prefix
    public Component getColoredName() {
        return TextComponentUtil.build(this, getName());
    }

    /// Gets the name of this color without coloring the returned result
    ///
    /// @return the color's name
    public MutableComponent getName() {
        return TextComponentUtil.translate(getTranslationKey());
    }

    /// Gets the corresponding text color for this color.
    public TextColor getColor() {
        return color;
    }

    @Override
    public EnumColor byIndex(int index) {
        return BY_ID.apply(index);
    }

    @Internal
    @Override
    public void setColorFromAtlas(int[] color) {
        this.rgbCode = color;
        this.argb = ARGB.color(0xFF, rgbCode[0], rgbCode[1], rgbCode[2]);
        this.color = TextColor.fromRgb(this.argb);
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

    @Override
    public String getSerializedName() {
        return registryPrefix;
    }

    @Override
    public String getTranslationKey() {
        return translationKey;
    }
}