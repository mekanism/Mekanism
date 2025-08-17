package mekanism.api.text;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import java.util.function.IntFunction;
import mekanism.api.IIncrementalEnum;
import mekanism.api.SupportsColorMap;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.FastColor;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Simple color enum for adding colors to in-game GUI strings of text.
 *
 * @author AidanBrady
 */
public enum EnumColor implements IIncrementalEnum<EnumColor>, SupportsColorMap, StringRepresentable {
    BLACK("§0", APILang.COLOR_BLACK, "Black", "black", new int[]{64, 64, 64}, DyeColor.BLACK),
    DARK_BLUE("§1", APILang.COLOR_DARK_BLUE, "Blue", "blue", new int[]{54, 107, 208}, DyeColor.BLUE),
    DARK_GREEN("§2", APILang.COLOR_DARK_GREEN, "Green", "green", new int[]{89, 193, 95}, DyeColor.GREEN),
    DARK_AQUA("§3", APILang.COLOR_DARK_AQUA, "Cyan", "cyan", new int[]{0, 243, 208}, DyeColor.CYAN),
    DARK_RED("§4", APILang.COLOR_DARK_RED, "Dark Red", "dark_red", new int[]{201, 7, 31}, MapColor.NETHER, null),
    PURPLE("§5", APILang.COLOR_PURPLE, "Purple", "purple", new int[]{164, 96, 217}, DyeColor.PURPLE),
    ORANGE("§6", APILang.COLOR_ORANGE, "Orange", "orange", new int[]{255, 161, 96}, DyeColor.ORANGE),
    GRAY("§7", APILang.COLOR_GRAY, "Light Gray", "light_gray", new int[]{207, 207, 207}, DyeColor.LIGHT_GRAY),
    DARK_GRAY("§8", APILang.COLOR_DARK_GRAY, "Gray", "gray", new int[]{122, 122, 122}, DyeColor.GRAY),
    INDIGO("§9", APILang.COLOR_INDIGO, "Light Blue", "light_blue", new int[]{85, 158, 255}, DyeColor.LIGHT_BLUE),
    BRIGHT_GREEN("§a", APILang.COLOR_BRIGHT_GREEN, "Lime", "lime", new int[]{117, 255, 137}, DyeColor.LIME),
    AQUA("§b", APILang.COLOR_AQUA, "Aqua", "aqua", new int[]{48, 255, 249}, MapColor.COLOR_LIGHT_BLUE, null),
    RED("§c", APILang.COLOR_RED, "Red", "red", new int[]{255, 56, 60}, DyeColor.RED),
    PINK("§d", APILang.COLOR_PINK, "Magenta", "magenta", new int[]{213, 94, 203}, DyeColor.MAGENTA),
    YELLOW("§e", APILang.COLOR_YELLOW, "Yellow", "yellow", new int[]{255, 221, 79}, DyeColor.YELLOW),
    WHITE("§f", APILang.COLOR_WHITE, "White", "white", new int[]{255, 255, 255}, DyeColor.WHITE),
    //Extras for dye-completeness
    BROWN("§6", APILang.COLOR_BROWN, "Brown", "brown", new int[]{161, 118, 73}, DyeColor.BROWN),
    BRIGHT_PINK("§d", APILang.COLOR_BRIGHT_PINK, "Pink", "pink", new int[]{255, 188, 196}, DyeColor.PINK);

    /**
     * Codec for serializing colors based on their name.
     *
     * @since 10.6.0
     */
    public static final Codec<EnumColor> CODEC = StringRepresentable.fromEnum(EnumColor::values);
    /**
     * Gets a color by index, wrapping for out of bounds indices.
     *
     * @since 10.6.0
     */
    public static final IntFunction<EnumColor> BY_ID = ByIdMap.continuous(EnumColor::ordinal, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
    /**
     * Stream codec for syncing colors by index.
     *
     * @since 10.6.0
     */
    public static final StreamCodec<ByteBuf, EnumColor> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, EnumColor::ordinal);
    /**
     * Stream codec for syncing optional colors by index.
     *
     * @since 10.6.0
     */
    public static final StreamCodec<ByteBuf, Optional<EnumColor>> OPTIONAL_STREAM_CODEC = ByteBufCodecs.optional(STREAM_CODEC);
    /**
     * The color code that will be displayed
     */
    public final String code;
    private final ILangEntry langEntry;
    private final String englishName;
    private final String registryPrefix;
    @Nullable
    private final DyeColor dyeColor;
    private final MapColor mapColor;

    private TextColor color;
    private int[] rgbCode;
    private int argb;

    EnumColor(String s, ILangEntry langEntry, String englishName, String registryPrefix, int[] rgbCode, DyeColor dyeColor) {
        this(s, langEntry, englishName, registryPrefix, rgbCode, dyeColor.getMapColor(), dyeColor);
    }

    EnumColor(String code, ILangEntry langEntry, String englishName, String registryPrefix, int[] rgbCode, MapColor mapColor, @Nullable DyeColor dyeColor) {
        this.code = code;
        this.langEntry = langEntry;
        this.englishName = englishName;
        this.dyeColor = dyeColor;
        this.registryPrefix = registryPrefix;
        setColorFromAtlas(rgbCode);
        this.mapColor = mapColor;
    }

    /**
     * Gets the prefix to use in registry names for this color.
     */
    public String getRegistryPrefix() {
        return registryPrefix;
    }

    /**
     * Gets the English name of this color.
     */
    public String getEnglishName() {
        return englishName;
    }

    /**
     * Gets the material or map color that most closely corresponds to this color.
     */
    public MapColor getMapColor() {
        return mapColor;
    }

    /**
     * Gets the corresponding dye color or {@code null} if there isn't one.
     */
    @Nullable
    public DyeColor getDyeColor() {
        return dyeColor;
    }

    /**
     * Gets the name of this color with its color prefix code.
     *
     * @return the color's name and color prefix
     */
    public Component getColoredName() {
        return TextComponentUtil.build(this, getName());
    }

    /**
     * Gets the name of this color without coloring the returned result
     *
     * @return the color's name
     */
    public MutableComponent getName() {
        return langEntry.translate();
    }

    /**
     * @apiNote For use by the data generators.
     */
    public ILangEntry getLangEntry() {
        return langEntry;
    }

    /**
     * Gets the corresponding text color for this color.
     */
    public TextColor getColor() {
        return color;
    }

    @Override
    public String toString() {
        return code;
    }

    @NotNull
    @Override
    public EnumColor byIndex(int index) {
        return BY_ID.apply(index);
    }

    /**
     * @apiNote This method is mostly for <strong>INTERNAL</strong> usage.
     */
    @Override
    public void setColorFromAtlas(int[] color) {
        rgbCode = color;
        this.argb = FastColor.ARGB32.color(rgbCode[0], rgbCode[1], rgbCode[2]);
        this.color = TextColor.fromRgb(this.argb);
    }

    @Override
    public int getPackedColor() {
        return argb;
    }

    /**
     * @apiNote Modifying the returned array will result in this color object changing the color it represents, and should not be done.
     */
    @Override
    public int[] getRgbCode() {
        return rgbCode;
    }

    @NotNull
    @Override
    public String getSerializedName() {
        return registryPrefix;
    }
}