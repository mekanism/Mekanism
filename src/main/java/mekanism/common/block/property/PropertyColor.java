package mekanism.common.block.property;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.EnumColor;
import mekanism.api.IColor;
import mekanism.common.util.TransporterUtils;
import net.minecraft.block.properties.AbstractProperty;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
//There are a good number of unchecked cast warnings due to the fact that forge has a hard class structure requirement of
// T extends Comparable<T> so we cannot just use PropertyHelper<IColor> or there will be type clashes in EnumColor due to
// enums extending Comparable as well
public class PropertyColor<T extends IColor<T>> extends AbstractProperty<T> {

    private final ImmutableSet<T> allowedValues;

    protected PropertyColor(String name, Class<T> colorClass, Collection<T> values) {
        super(name, colorClass);
        this.allowedValues = ImmutableSet.copyOf(values);
    }

    public static PropertyColor create(String name) {
        List<IColor> allowedValues = new ArrayList<>();
        allowedValues.add(EnumColor.NONE);
        Collections.addAll(allowedValues, EnumColor.values());
        return new PropertyColor<>(name, IColor.class, allowedValues);
    }

    public static PropertyColor createTransporter(String name) {
        List<IColor> allowedValues = new ArrayList<>();
        allowedValues.add(EnumColor.NONE);
        allowedValues.addAll(TransporterUtils.colors);
        return new PropertyColor<>(name, IColor.class, allowedValues);
    }

    @Nonnull
    @Override
    public Collection<T> getAllowedValues() {
        return allowedValues;
    }

    @Override
    public Optional<T> parseValue(String value) {
        T color = (T) EnumColor.NONE;
        for (EnumColor c : EnumColor.values()) {
            if (c.registry_prefix.equals(value)) {
                color = (T) c;
                break;
            }
        }
        return Optional.of(color);
    }

    @Override
    public String getName(IColor color) {
        return color.getRegistryPrefix();
    }
}