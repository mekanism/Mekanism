package mekanism.common.util;

import net.minecraft.util.EnumHand;

public class EnumUtils {

    public static EnumHand getHandSafe(int ordinal) {
        return getEnumSafe(EnumHand.values(), ordinal, EnumHand.MAIN_HAND);
    }

    public static <TYPE extends Enum<TYPE>> TYPE getEnumSafe(TYPE[] values, int ordinal, TYPE fallback) {
        if (ordinal < 0 || ordinal >= values.length) {
            return fallback;
        }
        return values[ordinal];
    }

    public static <TYPE extends Enum<TYPE>> TYPE nextValueWrap(TYPE[] values, int ordinal) {
        int nextOrdinal = ordinal + 1;
        if (nextOrdinal < 0 || nextOrdinal >= values.length) {
            return values[0];
        }
        return values[nextOrdinal];
    }
}