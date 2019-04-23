package mekanism.common.tier;

import java.util.Locale;
import mekanism.api.EnumColor;
import mekanism.common.util.LangUtils;
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

    private String name;
    private EnumColor color;

    BaseTier(String s, EnumColor c) {
        name = s;
        color = c;
    }

    public String getSimpleName() {
        return name;
    }

    public String getLocalizedName() {
        return LangUtils.localize("tier." + getSimpleName());
    }

    public EnumColor getColor() {
        return color;
    }

    public boolean isObtainable() {
        return this != CREATIVE;
    }

    @Override
    public String getName() {
        return name().toLowerCase(Locale.ROOT);
    }
}