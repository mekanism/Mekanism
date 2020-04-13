package mekanism.common.frequency;

import mekanism.api.math.MathUtils;

public enum FrequencyType {
    BASE,
    INVENTORY,
    SECURITY;

    private static final FrequencyType[] TYPES = values();

    public static FrequencyType byIndexStatic(int index) {
        return MathUtils.getByIndexMod(TYPES, index);
    }
}