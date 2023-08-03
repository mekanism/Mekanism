package mekanism.api.tier;

import java.util.Locale;
import mekanism.api.SupportsColorMap;
import mekanism.api.math.MathUtils;
import mekanism.api.text.EnumColor;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.NotNull;

/**
 * The default tiers used in Mekanism.
 *
 * @author aidancbrady
 */
public enum BaseTier implements StringRepresentable, SupportsColorMap {
    BASIC("Basic", EnumColor.BRIGHT_GREEN, EnumColor.BRIGHT_GREEN),
    ADVANCED("Advanced", EnumColor.DARK_RED, EnumColor.RED),
    ELITE("Elite", EnumColor.INDIGO, EnumColor.INDIGO),
    ULTIMATE("Ultimate", EnumColor.PURPLE, EnumColor.PURPLE),
    CREATIVE("Creative", EnumColor.BLACK, EnumColor.DARK_GRAY);

    private static final BaseTier[] TIERS = values();

    private final String name;
    private final EnumColor color;
    private final EnumColor textColor;
    private int[] rgbCode;

    BaseTier(String name, EnumColor color, EnumColor textColor) {
        this.name = name;
        this.color = color;
        this.textColor = textColor;
        //TODO - 1.20: Default this instead via parameter instead of via the enum color
        setColorFromAtlas(color.getRgbCode());
    }

    /**
     * Gets the name of this tier.
     */
    public String getSimpleName() {
        return name;
    }

    /**
     * Gets the lowercase name of this tier.
     */
    public String getLowerName() {
        return getSimpleName().toLowerCase(Locale.ROOT);
    }

    /**
     * Gets the map color that corresponds to this tier.
     *
     * @since 10.4.0
     */
    public MapColor getMapColor() {
        //TODO - 1.20: Update the colors?
        return color.getMapColor();
    }

    /**
     * {@inheritDoc}
     *
     * @apiNote Modifying the returned array will result in this color object changing the color it represents, and should not be done.
     */
    @Override
    public int[] getRgbCode() {
        return rgbCode;
    }

    /**
     * {@inheritDoc}
     *
     * @apiNote This method is mostly for <strong>INTERNAL</strong> usage.
     *
     * @since 10.4.0
     */
    @Override
    public void setColorFromAtlas(int[] color) {
        rgbCode = color;
    }

    /**
     * Gets the color that corresponds to this tier for use in text messages.
     */
    public EnumColor getTextColor() {
        return textColor;
    }

    @NotNull
    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }

    /**
     * Gets a tier by index.
     *
     * @param index Index of the tier.
     */
    public static BaseTier byIndexStatic(int index) {
        return MathUtils.getByIndexMod(TIERS, index);
    }
}