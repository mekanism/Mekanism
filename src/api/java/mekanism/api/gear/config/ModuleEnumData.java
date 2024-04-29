package mekanism.api.gear.config;

import java.util.List;
import java.util.Objects;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.text.IHasTextComponent;
import net.minecraft.nbt.CompoundTag;

/**
 * Enum based implementation of {@link ModuleConfigData}.
 *
 * @apiNote Does not currently support {@link mekanism.api.IDisableableEnum}.
 */
@NothingNullByDefault
public final class ModuleEnumData<TYPE extends Enum<TYPE> & IHasTextComponent> implements ModuleConfigData<TYPE> {

    private final List<TYPE> enumConstants;
    private final TYPE defaultValue;
    private TYPE value;

    /**
     * Creates a new {@link ModuleEnumData} initialized to the given default value.
     *
     * @param def Default value.
     *
     * @since 10.3.2
     */
    public ModuleEnumData(TYPE def) {
        this.value = this.defaultValue = Objects.requireNonNull(def, "Default value cannot be null.");
        this.enumConstants = List.of(this.defaultValue.getDeclaringClass().getEnumConstants());
    }

    /**
     * Creates a new {@link ModuleEnumData} out of the first selectableCount elements in the enum of the default value's type and initializes it to the given default
     * value.
     *
     * @param selectableCount The number of selectable elements.
     * @param def             Default value.
     *
     * @since 10.3.2
     */
    public ModuleEnumData(TYPE def, int selectableCount) {
        this.value = this.defaultValue = Objects.requireNonNull(def, "Default value cannot be null.");
        if (selectableCount <= 0) {
            throw new IllegalArgumentException("Invalid selectableCount, there must be at least one element that is selectable.");
        }
        Class<TYPE> enumClass = this.defaultValue.getDeclaringClass();
        TYPE[] constants = enumClass.getEnumConstants();
        if (constants.length < selectableCount) {
            throw new IllegalArgumentException("Selectable count is larger than the number of elements in " + enumClass.getSimpleName());
        } else if (constants.length == selectableCount) {
            this.enumConstants = List.of(constants);
        } else {
            if (this.defaultValue.ordinal() >= selectableCount) {
                throw new IllegalArgumentException("Invalid default, it is out of range of the selectable values.");
            }
            this.enumConstants = List.of(constants).subList(0, selectableCount);
        }
    }

    /**
     * Gets all the valid values of this {@link ModuleEnumData}.
     *
     * @implNote This list is immutable.
     */
    public List<TYPE> getEnums() {
        return enumConstants;
    }

    @Override
    public TYPE get() {
        return value;
    }

    @Override
    public void set(TYPE val) {
        Objects.requireNonNull(val, "Value cannot be null.");
        if (val.ordinal() >= enumConstants.size()) {
            throw new IllegalArgumentException("Invalid value, it is out of range of the selectable values.");
        }
        value = val;
    }

    @Override
    public void read(String name, CompoundTag tag) {
        Objects.requireNonNull(tag, "Tag cannot be null.");
        Objects.requireNonNull(name, "Name cannot be null.");
        int ordinal = tag.getInt(name);
        if (ordinal >= 0 && ordinal < enumConstants.size()) {
            value = enumConstants.get(ordinal);
        } else {
            value = defaultValue;
        }
    }

    @Override
    public void write(String name, CompoundTag tag) {
        Objects.requireNonNull(tag, "Tag cannot be null.");
        Objects.requireNonNull(name, "Name cannot be null.");
        tag.putInt(name, value.ordinal());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ModuleEnumData<?> other = (ModuleEnumData<?>) o;
        return enumConstants.equals(other.enumConstants) && defaultValue == other.defaultValue && value == other.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(enumConstants, defaultValue, value);
    }
}