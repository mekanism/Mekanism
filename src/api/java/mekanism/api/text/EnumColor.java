package mekanism.api.text;

import mekanism.api.IIncrementalEnum;
import mekanism.api.math.MathUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.material.MaterialColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Simple color enum for adding colors to in-game GUI strings of text.
 *
 * @author AidanBrady
 */
public enum EnumColor implements IIncrementalEnum<EnumColor> {
    BLACK("\u00a70", APILang.COLOR_BLACK, "Black", "black", new int[]{64, 64, 64}, DyeColor.BLACK),
    DARK_BLUE("\u00a71", APILang.COLOR_DARK_BLUE, "Blue", "blue", new int[]{54, 107, 208}, DyeColor.BLUE),
    DARK_GREEN("\u00a72", APILang.COLOR_DARK_GREEN, "Green", "green", new int[]{89, 193, 95}, DyeColor.GREEN),
    DARK_AQUA("\u00a73", APILang.COLOR_DARK_AQUA, "Cyan", "cyan", new int[]{0, 243, 208}, DyeColor.CYAN),
    DARK_RED("\u00a74", APILang.COLOR_DARK_RED, "Dark Red", "dark_red", new int[]{201, 7, 31}, MaterialColor.NETHER, null),
    PURPLE("\u00a75", APILang.COLOR_PURPLE, "Purple", "purple", new int[]{164, 96, 217}, DyeColor.PURPLE),
    ORANGE("\u00a76", APILang.COLOR_ORANGE, "Orange", "orange", new int[]{255, 161, 96}, DyeColor.ORANGE),
    GRAY("\u00a77", APILang.COLOR_GRAY, "Light Gray", "light_gray", new int[]{207, 207, 207}, DyeColor.LIGHT_GRAY),
    DARK_GRAY("\u00a78", APILang.COLOR_DARK_GRAY, "Gray", "gray", new int[]{122, 122, 122}, DyeColor.GRAY),
    INDIGO("\u00a79", APILang.COLOR_INDIGO, "Light Blue", "light_blue", new int[]{85, 158, 255}, DyeColor.LIGHT_BLUE),
    BRIGHT_GREEN("\u00a7a", APILang.COLOR_BRIGHT_GREEN, "Lime", "lime", new int[]{117, 255, 137}, DyeColor.LIME),
    AQUA("\u00a7b", APILang.COLOR_AQUA, "Aqua", "aqua", new int[]{48, 255, 249}, MaterialColor.COLOR_LIGHT_BLUE, null),
    RED("\u00a7c", APILang.COLOR_RED, "Red", "red", new int[]{255, 56, 60}, DyeColor.RED),
    PINK("\u00a7d", APILang.COLOR_PINK, "Magenta", "magenta", new int[]{213, 94, 203}, DyeColor.MAGENTA),
    YELLOW("\u00a7e", APILang.COLOR_YELLOW, "Yellow", "yellow", new int[]{255, 221, 79}, DyeColor.YELLOW),
    WHITE("\u00a7f", APILang.COLOR_WHITE, "White", "white", new int[]{255, 255, 255}, DyeColor.WHITE),
    //Extras for dye-completeness
    BROWN("\u00a76", APILang.COLOR_BROWN, "Brown", "brown", new int[]{161, 118, 73}, DyeColor.BROWN),
    BRIGHT_PINK("\u00a7d", APILang.COLOR_BRIGHT_PINK, "Pink", "pink", new int[]{255, 188, 196}, DyeColor.PINK);

    private static final EnumColor[] COLORS = values();
    /**
     * The color code that will be displayed
     */
    public final String code;

    private int[] rgbCode;
    private TextColor color;
    private final APILang langEntry;
    private final String englishName;
    private final String registryPrefix;
    @Nullable
    private final DyeColor dyeColor;
    private final MaterialColor mapColor;

    EnumColor(String s, APILang langEntry, String englishName, String registryPrefix, int[] rgbCode, DyeColor dyeColor) {
        this(s, langEntry, englishName, registryPrefix, rgbCode, dyeColor.getMaterialColor(), dyeColor);
    }

    EnumColor(String code, APILang langEntry, String englishName, String registryPrefix, int[] rgbCode, MaterialColor mapColor, @Nullable DyeColor dyeColor) {
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
    public MaterialColor getMapColor() {
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
    public APILang getLangEntry() {
        return langEntry;
    }

    /**
     * Gets the 0-1 of this color's RGB value by dividing by 255 (used for OpenGL coloring).
     *
     * @param index - R:0, G:1, B:2
     *
     * @return the color value
     */
    public float getColor(int index) {
        return rgbCode[index] / 255F;
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

    /**
     * Gets a color by index.
     *
     * @param index Index of the color.
     */
    public static EnumColor byIndexStatic(int index) {
        return MathUtils.getByIndexMod(COLORS, index);
    }

    @NotNull
    @Override
    public EnumColor byIndex(int index) {
        return byIndexStatic(index);
    }

    /**
     * Sets the internal color representation of this color from the color atlas.
     *
     * @param color Color data.
     *
     * @apiNote This method is mostly for <strong>INTERNAL</strong> usage.
     */
    public void setColorFromAtlas(int[] color) {
        rgbCode = color;
        this.color = TextColor.fromRgb(rgbCode[0] << 16 | rgbCode[1] << 8 | rgbCode[2]);
    }

    /**
     * Gets the red, green and blue color value, as an integer(range: 0 - 255).
     *
     * @return the color values.
     *
     * @apiNote Modifying the returned array will result in this color object changing the color it represents, and should not be done.
     */
    public int[] getRgbCode() {
        return rgbCode;
    }

    /**
     * Gets the red, green and blue color value, as a float(range: 0 - 1).
     *
     * @return the color values.
     */
    public float[] getRgbCodeFloat() {
        return new float[]{getColor(0), getColor(1), getColor(2)};
    }
}