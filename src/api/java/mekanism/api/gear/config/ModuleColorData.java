package mekanism.api.gear.config;

import mekanism.api.annotations.NothingNullByDefault;

/**
 * Integer implementation of {@link ModuleConfigData} for representing colors in ARGB format.
 *
 * @since 10.3.3
 */
@NothingNullByDefault
public final class ModuleColorData extends ModuleIntegerData {

    /**
     * Creates a new {@link ModuleColorData} that supports alpha and has a default value of white ({@code 0xFFFFFFFF}).
     *
     * @implNote Color format is ARGB.
     */
    public static ModuleColorData argb() {
        return argb(0xFFFFFFFF);
    }

    /**
     * Creates a new {@link ModuleColorData} that supports alpha and has the given default color.
     *
     * @param defaultColor Default color.
     *
     * @implNote Color format is ARGB.
     */
    public static ModuleColorData argb(int defaultColor) {
        return new ModuleColorData(defaultColor, true);
    }

    /**
     * Creates a new {@link ModuleColorData} that doesn't support alpha and has a default value of white ({@code 0xFFFFFFFF}).
     *
     * @implNote Color format is ARGB with the alpha component being locked to {@code 0xFF}.
     */
    public static ModuleColorData rgb() {
        return rgb(0xFFFFFFFF);
    }

    /**
     * Creates a new {@link ModuleColorData} that doesn't support alpha and has the given default color.
     *
     * @param defaultColor Default color.
     *
     * @implNote Color format is ARGB with the alpha component being locked to {@code 0xFF}.
     */
    public static ModuleColorData rgb(int defaultColor) {
        return new ModuleColorData(defaultColor, false);
    }

    private final boolean handlesAlpha;

    /**
     * Creates a new {@link ModuleColorData} with the given default value.
     *
     * @param defaultColor Default value.
     * @param handlesAlpha Whether this data supports changing the alpha component.
     */
    private ModuleColorData(int defaultColor, boolean handlesAlpha) {
        super(handlesAlpha ? defaultColor : defaultColor | 0xFF000000);
        this.handlesAlpha = handlesAlpha;
    }

    @Override
    protected int sanitizeValue(int value) {
        //If we don't handle alpha make sure we do have the alpha component present though
        return handlesAlpha ? value : value | 0xFF000000;
    }

    /**
     * Gets whether this {@link ModuleColorData} handles alpha, if it does not the color returned will fully opaque.
     *
     * @return {@code true} if this data can handle alpha.
     */
    public boolean handlesAlpha() {
        return handlesAlpha;
    }
}