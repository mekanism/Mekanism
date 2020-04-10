package mekanism.api.tier;

import java.util.Locale;
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

    private String name;
    private EnumColor color;

    BaseTier(String s, EnumColor c) {
        name = s;
        color = c;
    }

    public String getSimpleName() {
        return name;
    }

    //TODO: Maybe come up with a better method name for this
    public String getLowerName() {
        return getSimpleName().toLowerCase(Locale.ROOT);
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

    public static BaseTier byIndexStatic(int index) {
        //TODO: Is it more efficient to check if index is negative and then just do the normal mod way?
        return TIERS[Math.floorMod(index, TIERS.length)];
    }
}