package mekanism.common.util;

import javax.annotation.Nullable;
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

    @Nullable
    public static <TYPE extends Enum<TYPE>> TYPE nextValue(TYPE element) {
        int nextOrdinal = element.ordinal() + 1;
        Enum[] enumConstants = element.getClass().getEnumConstants();
        if (nextOrdinal >= enumConstants.length) {
            return null;
        }
        return (TYPE) enumConstants[nextOrdinal];
    }

    public static <TYPE extends Enum<TYPE>> TYPE nextValueWrap(TYPE element) {
        int nextOrdinal = element.ordinal() + 1;
        Enum[] enumConstants = element.getClass().getEnumConstants();
        if (nextOrdinal >= enumConstants.length) {
            return (TYPE) enumConstants[0];
        }
        return (TYPE) enumConstants[nextOrdinal];
    }
}