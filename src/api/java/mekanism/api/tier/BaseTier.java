package mekanism.api.tier;

import java.util.Locale;
import javax.annotation.Nonnull;
import mekanism.api.math.MathUtils;
import mekanism.api.text.EnumColor;
import net.minecraft.util.IStringSerializable;

/**
 * The default tiers used in Mekanism.
 *
 * @author aidancbrady
 */
public enum BaseTier implements IStringSerializable {
    BASIC("Basic", EnumColor.BRIGHT_GREEN, EnumColor.BRIGHT_GREEN),
    ADVANCED("Advanced", EnumColor.DARK_RED, EnumColor.RED),
    ELITE("Elite", EnumColor.INDIGO, EnumColor.INDIGO),
    ULTIMATE("Ultimate", EnumColor.PURPLE, EnumColor.PURPLE),
    CREATIVE("Creative", EnumColor.BLACK, EnumColor.DARK_GRAY);

    private static final BaseTier[] TIERS = values();

    private final String name;
    private final EnumColor color;
    private final EnumColor textColor;

    BaseTier(String s, EnumColor c, EnumColor c1) {
        name = s;
        color = c;
        textColor = c1;
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
     * Gets the color that corresponds to this tier for use in rendering.
     */
    public EnumColor getColor() {
        return color;
    }

    /**
     * Gets the color that corresponds to this tier for use in text messages.
     */
    public EnumColor getTextColor() {
        return textColor;
    }

    @Nonnull
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