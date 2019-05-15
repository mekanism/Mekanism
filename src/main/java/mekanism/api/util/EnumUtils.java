package mekanism.api.util;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Helpful utility methods for interacting with Enum Data. This is in the API package as a couple Enums we have in it use parts of it.
 */
public class EnumUtils {

    /**
     * @param values   The values of the enum.
     * @param ordinal  The index of the element in the enum we are trying to retrieve.
     * @param fallback The fallback value if ordinal is out of bounds.
     * @param <TYPE>   Some type of Enum.
     *
     * @return Enum value at the given ordinal or fallback if the ordinal is out of bounds.
     */
    public static <TYPE extends Enum<TYPE>> TYPE getEnumSafe(TYPE[] values, int ordinal, TYPE fallback) {
        if (ordinal < 0 || ordinal >= values.length) {
            return fallback;
        }
        return values[ordinal];
    }

    /**
     * @param values   The values of the enum.
     * @param ordinal  The index of the element in the enum we are trying to retrieve.
     * @param <TYPE>   Some type of Enum.
     *
     * @return Enum value at the given ordinal or null if the ordinal is out of bounds.
     */
    @Nullable
    public static <TYPE extends Enum<TYPE>> TYPE getEnumSafe(TYPE[] values, int ordinal) {
        return getEnumSafe(values, ordinal, null);
    }

    /**
     * Gets the Enum value after the given one.
     *
     * @param element An element in an enum.
     * @param <TYPE>  Some type of Enum.
     *
     * @return Element after the given one. Null if this is the last element in the Enum.
     */
    @Nullable
    public static <TYPE extends Enum<TYPE>> TYPE nextValue(@Nonnull TYPE element) {
        int nextOrdinal = element.ordinal() + 1;
        Enum[] enumConstants = element.getClass().getEnumConstants();
        if (nextOrdinal >= enumConstants.length) {
            return null;
        }
        return (TYPE) enumConstants[nextOrdinal];
    }

    /**
     * Gets the Enum value after the given one. Wraps around to the start if element is the last value of the Enum.
     *
     * @param element An element in an enum.
     * @param <TYPE>  Some type of Enum.
     *
     * @return Element after the given one. If this is the last one it returns the first element in the Enum instead.
     */
    public static <TYPE extends Enum<TYPE>> TYPE nextValueWrap(@Nonnull TYPE element) {
        int nextOrdinal = element.ordinal() + 1;
        Enum[] enumConstants = element.getClass().getEnumConstants();
        if (nextOrdinal >= enumConstants.length) {
            return (TYPE) enumConstants[0];
        }
        return (TYPE) enumConstants[nextOrdinal];
    }
}