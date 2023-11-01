package mekanism.common.lib;

import java.lang.reflect.Field;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import mekanism.common.Mekanism;
import net.neoforged.fml.util.ObfuscationReflectionHelper;
import net.neoforged.fml.util.ObfuscationReflectionHelper.UnableToFindFieldException;

/**
 * Helper  to make reflecting and grabbing the value of fields in a cached way easier
 *
 * @param <CLASS> Class the field is in.
 * @param <TYPE>  Type of the field.
 */
public class FieldReflectionHelper<CLASS, TYPE> {

    private final Class<CLASS> clazz;
    private final String fieldName;
    private final Supplier<TYPE> fallback;
    private Field field;

    public FieldReflectionHelper(Class<CLASS> clazz, String fieldName, Supplier<TYPE> fallback) {
        this.clazz = clazz;
        this.fieldName = fieldName;
        this.fallback = fallback;
    }

    public TYPE getValue(CLASS input) {
        if (field == null) {
            try {
                field = ObfuscationReflectionHelper.findField(clazz, fieldName);
            } catch (UnableToFindFieldException e) {
                Mekanism.logger.error("Error getting {} {} field.", clazz.getSimpleName(), fieldName, e);
                return fallback.get();
            }
        }
        try {
            return (TYPE) field.get(input);
        } catch (IllegalAccessException e) {
            Mekanism.logger.error("Error accessing {} {} field.", clazz.getSimpleName(), fieldName, e);
            return fallback.get();
        }
    }

    public void transformValue(CLASS input, Predicate<TYPE> shouldTransform, UnaryOperator<TYPE> valueTransformer) {
        TYPE value = getValue(input);
        //Field should not be null unless we just failed to get the value at which point there is no point to
        // try and transform the value or set it.
        // We also validate whether we should transform the value and try to set it so that we can skip if
        // it is equal to the fallback value or there is nothing to do without calling set on it afterwards
        if (field != null && shouldTransform.test(value)) {
            value = valueTransformer.apply(value);
            try {
                field.set(input, value);
            } catch (IllegalAccessException e) {
                Mekanism.logger.error("Error accessing {} {} field.", clazz.getSimpleName(), fieldName, e);
            }
        }
    }
}