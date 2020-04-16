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
    BASIC("Basic", EnumColor.BRIGHT_GREEN),
    ADVANCED("Advanced", EnumColor.DARK_RED),
    ELITE("Elite", EnumColor.DARK_BLUE),
    ULTIMATE("Ultimate", EnumColor.PURPLE),
    CREATIVE("Creative", EnumColor.BLACK);

    private static final BaseTier[] TIERS = values();

    private final String name;
    private final EnumColor color;

    BaseTier(String s, EnumColor c) {
        name = s;
        color = c;
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

    @Override
    public String getName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static BaseTier byIndexStatic(int index) {
        return MathUtils.getByIndexMod(TIERS, index);
    }
}