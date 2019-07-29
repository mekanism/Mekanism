package mekanism.api;

import java.util.Locale;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;

/**
 * Simple color enum for adding colors to in-game GUI strings of text.
 *
 * @author AidanBrady
 */
public enum EnumColor implements IStringSerializable {
    BLACK("\u00a70", "black", "Black", "black", new int[]{0, 0, 0}, 0, TextFormatting.BLACK),
    DARK_BLUE("\u00a71", "darkBlue", "Blue", "blue", new int[]{0, 0, 170}, 4, TextFormatting.DARK_BLUE),
    DARK_GREEN("\u00a72", "darkGreen", "Green", "green", new int[]{0, 170, 0}, 2, TextFormatting.DARK_GREEN),
    DARK_AQUA("\u00a73", "darkAqua", "Cyan", "cyan", new int[]{0, 255, 255}, 6, TextFormatting.DARK_AQUA),
    DARK_RED("\u00a74", "darkRed", null, "dark_red", new int[]{170, 0, 0}, -1, TextFormatting.DARK_RED),
    PURPLE("\u00a75", "purple", "Purple", "purple", new int[]{170, 0, 170}, 5, TextFormatting.DARK_PURPLE),
    ORANGE("\u00a76", "orange", "Orange", "orange", new int[]{255, 170, 0}, 14, TextFormatting.GOLD),
    GREY("\u00a77", "grey", "LightGray", "light_gray", new int[]{170, 170, 170}, 7, TextFormatting.GRAY),
    DARK_GREY("\u00a78", "darkGrey", "Gray", "gray", new int[]{85, 85, 85}, 8, TextFormatting.DARK_GRAY),
    INDIGO("\u00a79", "indigo", "LightBlue", "light_blue", new int[]{85, 85, 255}, 12, TextFormatting.BLUE),
    BRIGHT_GREEN("\u00a7a", "brightGreen", "Lime", "lime", new int[]{85, 255, 85}, 10, TextFormatting.GREEN),
    AQUA("\u00a7b", "aqua", null, "aqua", new int[]{85, 255, 255}, -1, TextFormatting.AQUA),
    RED("\u00a7c", "red", "Red", "red", new int[]{255, 0, 0}, 1, TextFormatting.RED),
    PINK("\u00a7d", "pink", "Magenta", "magenta", new int[]{255, 85, 255}, 13, TextFormatting.LIGHT_PURPLE),
    YELLOW("\u00a7e", "yellow", "Yellow", "yellow", new int[]{255, 255, 85}, 11, TextFormatting.YELLOW),
    WHITE("\u00a7f", "white", "White", "white", new int[]{255, 255, 255}, 15, TextFormatting.WHITE),
    //Extras for dye-completeness
    BROWN("\u00a76", "brown", "Brown", "brown", new int[]{150, 75, 0}, 3, TextFormatting.GOLD),
    BRIGHT_PINK("\u00a7d", "brightPink", "Pink", "pink", new int[]{255, 192, 203}, 9, TextFormatting.LIGHT_PURPLE);

    public static EnumColor[] DYES = new EnumColor[]{BLACK, RED, DARK_GREEN, BROWN, DARK_BLUE, PURPLE, DARK_AQUA, GREY, DARK_GREY, BRIGHT_PINK, BRIGHT_GREEN, YELLOW,
                                                     INDIGO, PINK, ORANGE, WHITE};

    /**
     * The color code that will be displayed
     */
    public final String code;

    public final int[] rgbCode;

    public final int mcMeta;
    public final TextFormatting textFormatting;
    /**
     * A friendly name of the color.
     */
    public String unlocalizedName;
    public final String registry_prefix;
    public String dyeName;

    EnumColor(String s, String unlocalized, String dye, String registry_prefix, int[] rgb, int meta, TextFormatting tf) {
        code = s;
        unlocalizedName = unlocalized;
        dyeName = dye;
        this.registry_prefix = registry_prefix;
        rgbCode = rgb;
        mcMeta = meta;
        textFormatting = tf;
    }

    public static EnumColor getFromDyeName(String s) {
        for (EnumColor c : values()) {
            if (c.dyeName.equalsIgnoreCase(s)) {
                return c;
            }
        }
        return null;
    }

    /**
     * Gets the localized name of this color by translating the unlocalized name.
     *
     * @return localized name
     */
    public String getLocalizedName() {
        return I18n.translateToLocal("color." + unlocalizedName);
    }

    @Deprecated
    public String getUnlocalizedName() {
        return getTranslationKey();
    }

    public String getTranslationKey() {
        return "color." + unlocalizedName;
    }

    public String getDyeName() {
        return I18n.translateToLocal("dye." + unlocalizedName);
    }

    public String getOreDictName() {
        return dyeName;
    }

    /**
     * Gets the name of this color with it's color prefix code.
     *
     * @return the color's name and color prefix
     */
    public String getColoredName() {
        return code + getLocalizedName();
    }

    public ITextComponent getTranslatedColouredComponent() {
        ITextComponent t = new TextComponentTranslation(getTranslationKey());
        t.getStyle().setColor(textFormatting);
        return t;
    }

    public String getDyedName() {
        return code + getDyeName();
    }

    @Override
    public String getName() {
        return unlocalizedName.toLowerCase(Locale.ROOT);
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

    /**
     * Gets the value of this color mapped to MC in-game item colors present in dyes and wool.
     *
     * @return mc meta value
     */
    public int getMetaValue() {
        return mcMeta;
    }

    @Override
    public String toString() {
        return code;
    }
}