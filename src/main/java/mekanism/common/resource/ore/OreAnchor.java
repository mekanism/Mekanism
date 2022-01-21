package mekanism.common.resource.ore;

import mekanism.common.world.height.AnchorType;

public record OreAnchor(AnchorType type, int value) {

    public static OreAnchor absolute(int value) {
        return new OreAnchor(AnchorType.ABSOLUTE, value);
    }

    public static OreAnchor aboveBottom(int value) {
        return new OreAnchor(AnchorType.ABOVE_BOTTOM, value);
    }

    public static OreAnchor belowTop(int value) {
        return new OreAnchor(AnchorType.BELOW_TOP, value);
    }
}