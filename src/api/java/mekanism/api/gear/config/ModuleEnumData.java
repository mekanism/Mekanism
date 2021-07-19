package mekanism.api.gear.config;

import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.math.MathUtils;
import mekanism.api.text.IHasTextComponent;
import net.minecraft.nbt.CompoundNBT;

/**
 * Enum based implementation of {@link ModuleConfigData}.
 *
 * @apiNote Does not currently support {@link mekanism.api.IDisableableEnum}.
 */
@ParametersAreNonnullByDefault
public final class ModuleEnumData<TYPE extends Enum<TYPE> & IHasTextComponent> implements ModuleConfigData<TYPE> {

    private final List<TYPE> enumConstants;
    private TYPE value;

    /**
     * Creates a new {@link ModuleEnumData} out of a given enum type and initializes it to the given default value.
     *
     * @param enumClass Class of the Enum this {@link ModuleEnumData} corresponds to.
     * @param def       Default value.
     */
    public ModuleEnumData(Class<TYPE> enumClass, TYPE def) {
        TYPE[] constants = Objects.requireNonNull(enumClass, "Enum Class cannot be null.").getEnumConstants();
        this.enumConstants = ImmutableList.<TYPE>builder()
              .addAll(Arrays.asList(constants))
              .build();
        this.value = Objects.requireNonNull(def, "Default value cannot be null.");
    }

    /**
     * Creates a new {@link ModuleEnumData} out of the first selectableCount elements in the given enum type and initializes it to the given default value.
     *
     * @param enumClass       Class of the Enum this {@link ModuleEnumData} corresponds to.
     * @param selectableCount The number of selectable elements.
     * @param def             Default value.
     */
    public ModuleEnumData(Class<TYPE> enumClass, int selectableCount, TYPE def) {
        TYPE[] constants = Objects.requireNonNull(enumClass, "Enum Class cannot be null.").getEnumConstants();
        if (selectableCount <= 0) {
            throw new IllegalArgumentException("Invalid selectableCount, there must be at least one element that is selectable.");
        }
        Objects.requireNonNull(def, "Default value cannot be null.");
        if (constants.length < selectableCount) {
            throw new IllegalArgumentException("Selectable count is larger than the number of elements in " + enumClass.getSimpleName());
        } else if (constants.length == selectableCount) {
            this.enumConstants = ImmutableList.<TYPE>builder()
                  .addAll(Arrays.asList(constants))
                  .build();
            this.value = def;
        } else {
            if (def.ordinal() >= selectableCount) {
                throw new IllegalArgumentException("Invalid default, it is out of range of the selectable values.");
            }
            this.enumConstants = ImmutableList.<TYPE>builder()
                  .addAll(Arrays.asList(constants).subList(0, selectableCount))
                  .build();
            this.value = def;
        }
    }

    /**
     * Gets all the valid values of this {@link ModuleEnumData}.
     *
     * @implNote This list is immutable.
     */
    @Nonnull
    public List<TYPE> getEnums() {
        return enumConstants;
    }

    @Nonnull
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
    public void read(String name, CompoundNBT tag) {
        Objects.requireNonNull(tag, "Tag cannot be null.");
        Objects.requireNonNull(name, "Name cannot be null.");
        value = MathUtils.getByIndexMod(enumConstants, tag.getInt(name));
    }

    @Override
    public void write(String name, CompoundNBT tag) {
        Objects.requireNonNull(tag, "Tag cannot be null.");
        Objects.requireNonNull(name, "Name cannot be null.");
        tag.putInt(name, value.ordinal());
    }
}