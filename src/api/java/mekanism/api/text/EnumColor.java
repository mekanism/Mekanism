package mekanism.api.text;

import java.util.Locale;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.item.DyeColor;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

/**
 * Simple color enum for adding colors to in-game GUI strings of text.
 *
 * @author AidanBrady
 */
public enum EnumColor implements IStringSerializable {
    BLACK("\u00a70", "black", "Black", "black", new int[]{0, 0, 0}, TextFormatting.BLACK, DyeColor.BLACK),
    DARK_BLUE("\u00a71", "darkBlue", "Blue", "blue", new int[]{0, 0, 170}, TextFormatting.DARK_BLUE, DyeColor.BLUE),
    DARK_GREEN("\u00a72", "darkGreen", "Green", "green", new int[]{0, 170, 0}, TextFormatting.DARK_GREEN, DyeColor.GREEN),
    DARK_AQUA("\u00a73", "darkAqua", "Cyan", "cyan", new int[]{0, 255, 255}, TextFormatting.DARK_AQUA, DyeColor.CYAN),
    DARK_RED("\u00a74", "darkRed", null, "dark_red", new int[]{170, 0, 0}, TextFormatting.DARK_RED, MaterialColor.NETHERRACK),
    PURPLE("\u00a75", "purple", "Purple", "purple", new int[]{170, 0, 170}, TextFormatting.DARK_PURPLE, DyeColor.PURPLE),
    ORANGE("\u00a76", "orange", "Orange", "orange", new int[]{255, 170, 0}, TextFormatting.GOLD, DyeColor.ORANGE),
    GRAY("\u00a77", "gray", "LightGray", "light_gray", new int[]{170, 170, 170}, TextFormatting.GRAY, DyeColor.LIGHT_GRAY),
    DARK_GRAY("\u00a78", "darkGray", "Gray", "gray", new int[]{85, 85, 85}, TextFormatting.DARK_GRAY, DyeColor.GRAY),
    INDIGO("\u00a79", "indigo", "LightBlue", "light_blue", new int[]{85, 85, 255}, TextFormatting.BLUE, DyeColor.LIGHT_BLUE),
    BRIGHT_GREEN("\u00a7a", "brightGreen", "Lime", "lime", new int[]{85, 255, 85}, TextFormatting.GREEN, DyeColor.LIME),
    AQUA("\u00a7b", "aqua", null, "aqua", new int[]{85, 255, 255}, TextFormatting.AQUA, MaterialColor.LIGHT_BLUE),
    RED("\u00a7c", "red", "Red", "red", new int[]{255, 0, 0}, TextFormatting.RED, DyeColor.RED),
    PINK("\u00a7d", "pink", "Magenta", "magenta", new int[]{255, 85, 255}, TextFormatting.LIGHT_PURPLE, DyeColor.MAGENTA),
    YELLOW("\u00a7e", "yellow", "Yellow", "yellow", new int[]{255, 255, 85}, TextFormatting.YELLOW, DyeColor.YELLOW),
    WHITE("\u00a7f", "white", "White", "white", new int[]{255, 255, 255}, TextFormatting.WHITE, DyeColor.WHITE),
    //Extras for dye-completeness
    BROWN("\u00a76", "brown", "Brown", "brown", new int[]{150, 75, 0}, TextFormatting.GOLD, DyeColor.BROWN),
    BRIGHT_PINK("\u00a7d", "brightPink", "Pink", "pink", new int[]{255, 192, 203}, TextFormatting.LIGHT_PURPLE, DyeColor.PINK);

    public static EnumColor[] DYES = new EnumColor[]{BLACK, RED, DARK_GREEN, BROWN, DARK_BLUE, PURPLE, DARK_AQUA, GRAY, DARK_GRAY, BRIGHT_PINK, BRIGHT_GREEN, YELLOW,
                                                     INDIGO, PINK, ORANGE, WHITE};

    /**
     * The color code that will be displayed
     */
    public final String code;

    public final int[] rgbCode;

    public final TextFormatting textFormatting;
    /**
     * A friendly name of the color.
     */
    public final String unlocalizedName;
    //TODO: changed unlocalized name to use registry prefix
    public final String registry_prefix;
    public final String dyeName;
    private final MaterialColor mapColor;

    EnumColor(String s, String unlocalized, String dye, String registry_prefix, int[] rgb, TextFormatting tf, DyeColor dyeColor) {
        this(s, unlocalized, dye, registry_prefix, rgb, tf, dyeColor.getMapColor());
    }

    EnumColor(String s, String unlocalized, String dye, String registry_prefix, int[] rgb, TextFormatting tf, MaterialColor mapColor) {
        code = s;
        unlocalizedName = unlocalized;
        dyeName = dye;
        this.registry_prefix = registry_prefix;
        rgbCode = rgb;
        textFormatting = tf;
        this.mapColor = mapColor;
    }

    public static EnumColor getFromDyeName(String s) {
        for (EnumColor c : values()) {
            if (c.dyeName.equalsIgnoreCase(s)) {
                return c;
            }
        }
        return null;
    }

    public String getEnglishName() {
        switch (this) {
            case BLACK:
                return "Black";
            case DARK_BLUE:
                return "Blue";
            case DARK_GREEN:
                return "Green";
            case DARK_AQUA:
                return "Cyan";
            case DARK_RED:
                return "Dark Red";
            case PURPLE:
                return "Purple";
            case ORANGE:
                return "Orange";
            case GRAY:
                return "Light Gray";
            case DARK_GRAY:
                return "Gray";
            case INDIGO:
                return "Light Blue";
            case BRIGHT_GREEN:
                return "Lime";
            case AQUA:
                return "Aqua";
            case RED:
                return "Red";
            case PINK:
                return "Magenta";
            case YELLOW:
                return "Yellow";
            case WHITE:
                return "White";
            case BROWN:
                return "Brown";
            case BRIGHT_PINK:
                return "Pink";
        }
        return "invalid";
    }

    //TODO: This should probably be color.mekanism??
    //Note: Do not implement IHasTranslationKey as we want the default behaviour of EnumColor to be formatting
    public String getTranslationKey() {
        return "color." + unlocalizedName;
    }

    public MaterialColor getMapColor() {
        return mapColor;
    }

    /**
     * Gets the name of this color with it's color prefix code.
     *
     * @return the color's name and color prefix
     */
    public ITextComponent getColoredName() {
        return new TranslationTextComponent(getTranslationKey()).applyTextStyle(textFormatting);
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

    @Override
    public String toString() {
        return code;
    }
}