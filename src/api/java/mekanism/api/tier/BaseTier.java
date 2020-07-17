package mekanism.api.tier;

import java.util.Locale;
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

    public String getSimpleName() {
        return name;
    }

    public String getLowerName() {
        return getSimpleName().toLowerCase(Locale.ROOT);
    }

    public EnumColor getColor() {
        return color;
    }

    public EnumColor getTextColor() {
        return textColor;
    }

    @Override
    public String getString() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static BaseTier byIndexStatic(int index) {
        return MathUtils.getByIndexMod(TIERS, index);
    }
}