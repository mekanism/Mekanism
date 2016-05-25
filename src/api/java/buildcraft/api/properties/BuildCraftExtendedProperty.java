package buildcraft.api.properties;

import java.util.List;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

public class BuildCraftExtendedProperty<T extends Comparable<T>> extends BuildCraftProperty<T> implements IUnlistedProperty<T> {
    private final Predicate<T> function;

    public BuildCraftExtendedProperty(String name, Class<T> clazz, Predicate<T> allowingFunction) {
        super(name, clazz);
        function = allowingFunction;
    }

    public BuildCraftExtendedProperty(String name, Class<T> clazz, List<T> allowedValues) {
        super(name, clazz, allowedValues);
        function = null;
    }

    public static BuildCraftExtendedProperty<Double> create(String name, final double min, final double max) {
        return new BuildCraftExtendedProperty<Double>(name, Double.class, new Predicate<Double>() {
            @Override
            public boolean apply(Double input) {
                return input >= min && input <= max;
            }
        });
    }

    /** This is useful if you want to use a particular class for this property, but don't care what the class contains
     * (it will never contain an incorrect value) */
    public static <T extends Comparable<T>> BuildCraftExtendedProperty<T> createExtended(String name, Class clazz) {
        return new BuildCraftExtendedProperty<T>(name, clazz, Predicates.<T> alwaysTrue());
    }

    @Override
    public boolean isValid(T value) {
        if (function == null) {
            return values.contains(value);
        }
        return function.apply(value);
    }

    @Override
    public Class<T> getType() {
        return getValueClass();
    }

    @Override
    public String valueToString(T value) {
        return valueName(value);
    }

    public T getUnlistedValue(IExtendedBlockState state) {
        return state.getValue(asUnlistedProperty());
    }

    // Helper methods for arguments
    public IUnlistedProperty<T> asUnlistedProperty() {
        return this;
    }
}
