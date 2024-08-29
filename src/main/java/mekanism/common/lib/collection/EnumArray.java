package mekanism.common.lib.collection;

import java.util.EnumSet;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Brute force array with enum 'keys' using ordinals.
 * Allows integer iteration of values.
 * Null entries count as empty.
 * Null 'key' not allowed.
 */
public class EnumArray<ENUM extends Enum<ENUM>, VALUE> {
    private final ENUM[] enumValues;
    private final Object[] values;
    private final Class<ENUM> enumClass;

    public EnumArray(ENUM[] enumValues, Class<ENUM> enumClass) {
        this.enumValues = enumValues;
        values = new Object[enumValues.length];
        this.enumClass = enumClass;
    }

    @SuppressWarnings("unchecked")
    public @Nullable VALUE get(@NotNull ENUM enumValue) {
        return (VALUE) values[enumValue.ordinal()];
    }

    public void set(@NotNull ENUM enumValue, @Nullable VALUE value) {
        values[enumValue.ordinal()] = value;
    }

    public int countNonEmpty() {
        int size = 0;
        for (Object value : values) {
            if (value != null) {
                size++;
            }
        }
        return size;
    }

    public ENUM[] enumKeys() {
        return enumValues;
    }

    public Set<ENUM> usedKeys() {
        Set<ENUM> keys = EnumSet.noneOf(enumClass);
        Object[] v = values;
        for (int i = 0; i < v.length; i++) {
            if (v[i] != null) {
                keys.add(enumValues[i]);
            }
        }
        return keys;
    }

    public boolean isEmpty() {
        for (Object o : values) {
            if (o != null) {
                return false;
            }
        }
        return true;
    }

    /**
     * Puts any non-null values from the other array into this one, overriding any existing.
     * @param other the EnumArray to copy from
     */
    public void putAll(@NotNull EnumArray<ENUM, VALUE> other) {
        Object[] otherValues = other.values;
        for (int i = 0; i < otherValues.length; i++) {
            if (otherValues[i] != null) {
                values[i] = otherValues[i];
            }
        }
    }

}
