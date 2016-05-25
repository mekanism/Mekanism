package buildcraft.api.properties;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import com.google.common.base.Optional;

import net.minecraft.block.properties.PropertyHelper;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.MathHelper;

/** This class exists primarily to allow for a property to be used as either a normal IProperty, or an
 * IUnlistedProperty. It also exists to give IProperty's generic types. */
public class BuildCraftProperty<T extends Comparable<T>> extends PropertyHelper<T> {
    private final String name;
    private final Class<T> clazz;
    protected final List<T> values;

    public BuildCraftProperty(String name, Class<T> clazz, T[] values) {
        this(name, clazz, Arrays.asList(values));
    }

    public BuildCraftProperty(String name, Class<T> clazz, List<T> values) {
        super(name, clazz);
        this.name = name;
        this.clazz = clazz;
        this.values = values;
    }

    /** Used for BuildCraftInifiniteProperty */
    protected BuildCraftProperty(String name, Class<T> clazz) {
        super(name, clazz);
        this.name = name;
        this.clazz = clazz;
        this.values = Collections.emptyList();
    }

    public static <E extends Enum<E>> BuildCraftProperty<E> create(String name, Class<E> enumeration) {
        List<E> values = Arrays.asList(enumeration.getEnumConstants());
        return new BuildCraftProperty<E>(name, enumeration, values);
    }

    public static <E extends Enum<E>> BuildCraftProperty<E> create(String name, E... values) {
        Class<E> clazz = values[0].getDeclaringClass();
        List<E> list = Arrays.asList(values);
        return new BuildCraftProperty<E>(name, clazz, list);
    }

    public static BuildCraftProperty<Boolean> create(String name, boolean first) {
        return new BuildCraftProperty<Boolean>(name, Boolean.class, new Boolean[] { first, !first });
    }

    /** first and last are both inclusive values (use 0, 4 to create an array of [0, 1, 2, 3, 4]) */
    public static BuildCraftProperty<Integer> create(String name, int first, int last) {
        return create(name, first, last, 1);
    }

    /** first and last are both inclusive values (use 0, 12, 3 to create an array of [0, 3, 6, 9, 12]) */
    public static BuildCraftProperty<Integer> create(String name, int first, int last, int difference) {
        int actualDiff = Math.abs(difference);
        int number = MathHelper.floor_float(Math.abs(first - last) / (float) actualDiff + 1);
        Integer[] array = new Integer[number];
        int addedDiff = actualDiff * (first > last ? -1 : 1);
        int current = first;
        for (int i = 0; i < array.length; i++) {
            array[i] = current;
            current += addedDiff;
        }
        return new BuildCraftProperty<Integer>(name, Integer.class, array);
    }

    public static BuildCraftProperty<Double> create(String name, double first, double last) {
        return create(name, first, last, 1);
    }

    public static BuildCraftProperty<Double> create(String name, double first, double last, double difference) {
        double actualDiff = Math.abs(difference);
        Double[] array = new Double[(int) (Math.abs(first - last) / actualDiff)];
        double addedDiff = actualDiff * (first > last ? -1 : 1);
        for (int i = 0; i <= array.length; i++) {
            array[i] = first + (first - last) / addedDiff * i;
        }
        return new BuildCraftProperty<Double>(name, Double.class, array);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<T> getAllowedValues() {
        return values;
    }

    @Override
    public Class<T> getValueClass() {
        return clazz;
    }

    @Override
    public String getName(@SuppressWarnings("rawtypes") Comparable value) {
        return valueName(value);
    }

    protected String valueName(Object value) {
        if (value == null) {
            return "null";
        } else if (value instanceof IStringSerializable) {
            return ((IStringSerializable) value).getName();
        } else if (value instanceof Enum) {
            return ((Enum<?>) value).name().toLowerCase(Locale.ROOT);
        } else {
            return value.toString().toLowerCase(Locale.ROOT);
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("BuildCraftProperty [name=");
        builder.append(name);
        builder.append(", clazz=");
        builder.append(clazz);
        builder.append(", values=");
        builder.append(values);
        builder.append("]");
        return builder.toString();
    }

    @Override
    public Optional<T> parseValue(String value) {
        if (clazz.isEnum()) {
            return parseEnum(value, (BuildCraftProperty) this);
        }
        return Optional.absent();
    }

    public static <E extends Enum<E>> Optional<E> parseEnum(String name, BuildCraftProperty<E> prop) {
        for (E e : prop.values) {
            if (e.name().equalsIgnoreCase(name)) return Optional.of(e);
        }
        return Optional.absent();
    }
}
