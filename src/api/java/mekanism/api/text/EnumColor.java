package mekanism.api.text;

import mekanism.api.math.MathUtils;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.item.DyeColor;
import net.minecraft.item.Item;
import net.minecraft.tags.Tag;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.Tags;

/**
 * Simple color enum for adding colors to in-game GUI strings of text.
 *
 * @author AidanBrady
 */
public enum EnumColor {
    BLACK("\u00a70", APILang.COLOR_BLACK, "Black", "Black", "black", new int[]{0, 0, 0}, TextFormatting.BLACK, DyeColor.BLACK),
    DARK_BLUE("\u00a71", APILang.COLOR_DARK_BLUE, "Blue", "Blue", "blue", new int[]{0, 0, 170}, TextFormatting.DARK_BLUE, DyeColor.BLUE),
    DARK_GREEN("\u00a72", APILang.COLOR_DARK_GREEN, "Green", "Green", "green", new int[]{0, 170, 0}, TextFormatting.DARK_GREEN, DyeColor.GREEN),
    DARK_AQUA("\u00a73", APILang.COLOR_DARK_AQUA, "Cyan", "Cyan", "cyan", new int[]{0, 255, 255}, TextFormatting.DARK_AQUA, DyeColor.CYAN),
    DARK_RED("\u00a74", APILang.COLOR_DARK_RED, "Dark Red", null, "dark_red", new int[]{170, 0, 0}, TextFormatting.DARK_RED,
          MaterialColor.NETHERRACK, Tags.Items.DYES_RED),
    PURPLE("\u00a75", APILang.COLOR_PURPLE, "Purple", "Purple", "purple", new int[]{170, 0, 170}, TextFormatting.DARK_PURPLE, DyeColor.PURPLE),
    ORANGE("\u00a76", APILang.COLOR_ORANGE, "Orange", "Orange", "orange", new int[]{255, 170, 0}, TextFormatting.GOLD, DyeColor.ORANGE),
    GRAY("\u00a77", APILang.COLOR_GRAY, "Light Gray", "LightGray", "light_gray", new int[]{170, 170, 170}, TextFormatting.GRAY, DyeColor.LIGHT_GRAY),
    DARK_GRAY("\u00a78", APILang.COLOR_DARK_GRAY, "Gray", "Gray", "gray", new int[]{85, 85, 85}, TextFormatting.DARK_GRAY, DyeColor.GRAY),
    INDIGO("\u00a79", APILang.COLOR_INDIGO, "Light Blue", "LightBlue", "light_blue", new int[]{85, 85, 255}, TextFormatting.BLUE, DyeColor.LIGHT_BLUE),
    BRIGHT_GREEN("\u00a7a", APILang.COLOR_BRIGHT_GREEN, "Lime", "Lime", "lime", new int[]{85, 255, 85}, TextFormatting.GREEN, DyeColor.LIME),
    AQUA("\u00a7b", APILang.COLOR_AQUA, "Aqua", null, "aqua", new int[]{85, 255, 255}, TextFormatting.AQUA, MaterialColor.LIGHT_BLUE, Tags.Items.DYES_LIGHT_BLUE),
    RED("\u00a7c", APILang.COLOR_RED, "Red", "Red", "red", new int[]{255, 0, 0}, TextFormatting.RED, DyeColor.RED),
    PINK("\u00a7d", APILang.COLOR_PINK, "Magenta", "Magenta", "magenta", new int[]{255, 85, 255}, TextFormatting.LIGHT_PURPLE, DyeColor.MAGENTA),
    YELLOW("\u00a7e", APILang.COLOR_YELLOW, "Yellow", "Yellow", "yellow", new int[]{255, 255, 85}, TextFormatting.YELLOW, DyeColor.YELLOW),
    WHITE("\u00a7f", APILang.COLOR_WHITE, "White", "White", "white", new int[]{255, 255, 255}, TextFormatting.WHITE, DyeColor.WHITE),
    //Extras for dye-completeness
    BROWN("\u00a76", APILang.COLOR_BROWN, "Brown", "Brown", "brown", new int[]{150, 75, 0}, TextFormatting.GOLD, DyeColor.BROWN),
    BRIGHT_PINK("\u00a7d", APILang.COLOR_BRIGHT_PINK, "Pink", "Pink", "pink", new int[]{255, 192, 203}, TextFormatting.LIGHT_PURPLE, DyeColor.PINK);

    private static final EnumColor[] COLORS = values();
    /**
     * The color code that will be displayed
     */
    public final String code;

    public final int[] rgbCode;

    public final TextFormatting textFormatting;
    private final APILang langEntry;
    private final String englishName;
    private final String registryPrefix;
    private final String dyeName;
    private final MaterialColor mapColor;
    private final Tag<Item> dyeTag;

    EnumColor(String s, APILang langEntry, String englishName, String dyeName, String registryPrefix, int[] rgbCode, TextFormatting textFormatting, DyeColor dyeColor) {
        this(s, langEntry, englishName, dyeName, registryPrefix, rgbCode, textFormatting, dyeColor.getMapColor(), dyeColor.getTag());
    }

    EnumColor(String code, APILang langEntry, String englishName, String dyeName, String registryPrefix, int[] rgbCode, TextFormatting textFormatting,
          MaterialColor mapColor, Tag<Item> dyeTag) {
        this.code = code;
        this.langEntry = langEntry;
        this.englishName = englishName;
        this.dyeName = dyeName;
        this.registryPrefix = registryPrefix;
        this.rgbCode = rgbCode;
        this.textFormatting = textFormatting;
        this.mapColor = mapColor;
        this.dyeTag = dyeTag;
    }

    public String getRegistryPrefix() {
        return registryPrefix;
    }

    public String getEnglishName() {
        return englishName;
    }

    public MaterialColor getMapColor() {
        return mapColor;
    }

    public Tag<Item> getDyeTag() {
        return dyeTag;
    }

    /**
     * Gets the name of this color with it's color prefix code.
     *
     * @return the color's name and color prefix
     */
    public ITextComponent getColoredName() {
        return getName().applyTextStyle(textFormatting);
    }

    /**
     * Gets the name of this color without coloring the returned result
     *
     * @return the color's name
     */
    public ITextComponent getName() {
        return new TranslationTextComponent(langEntry.getTranslationKey());
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
        return (float) rgbCode[index] / 255F;
    }

    @Override
    public String toString() {
        return code;
    }

    public static EnumColor byIndexStatic(int index) {
        return MathUtils.getByIndexMod(COLORS, index);
    }
}