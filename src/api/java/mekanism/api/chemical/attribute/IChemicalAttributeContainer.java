package mekanism.api.chemical.attribute;

import java.util.Collection;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.ToDoubleBiFunction;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntBiFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongBiFunction;
import java.util.function.ToLongFunction;
import org.jetbrains.annotations.Nullable;

/**
 * Container class for helping manage and interact with attributes relating to chemicals.
 *
 * @since 10.3.9
 */
@SuppressWarnings("removal")
public interface IChemicalAttributeContainer<SELF extends IChemicalAttributeContainer<SELF>> {//TODO - 1.22: Re-evaluate, but most likely we want to remove this

    /**
     * Whether this chemical has an attribute of a certain type.
     *
     * @param type The type of the attribute to check for.
     *
     * @return if this chemical has the attribute.
     *
     * @implNote This method will try to check the attribute, and convert any modern attributes into the legacy type so that they can be returned by this method.
     */
    @Deprecated(forRemoval = true, since = "10.7.11")
    boolean has(Class<? extends ChemicalAttribute> type);

    /**
     * Whether this chemical has a legacy attribute of a certain type. This method explicitly only checks legacy (in code) defined attributes, and will return false in
     * cases where there is only a modern attribute declared via a data map.
     *
     * @param type The type of the attribute to check for.
     *
     * @return if this chemical has the attribute.
     *
     * @since 10.7.11
     */
    @SuppressWarnings("removal")
    @Deprecated(forRemoval = true, since = "10.7.11")
    boolean hasLegacy(Class<? extends ChemicalAttribute> type);

    /**
     * Gets the attribute instance of a certain type, or null if it doesn't exist.
     *
     * @param type The type of the attribute to get.
     *
     * @return attribute instance.
     *
     * @implNote This method will try to retrieve the attribute, and convert any modern attributes into the legacy type so that they can be returned by this method.
     */
    @Nullable
    @Deprecated(forRemoval = true, since = "10.7.11")
    <ATTRIBUTE extends ChemicalAttribute> ATTRIBUTE get(Class<ATTRIBUTE> type);

    /**
     * Gets the legacy attribute instance of a certain type, or null if it doesn't exist. This method explicitly only checks legacy (in code) defined attributes, and
     * will return null in cases where there is only a modern attribute declared via a data map.
     *
     * @param type The type of the attribute to get.
     *
     * @return attribute instance.
     *
     * @since 10.7.11
     */
    @Nullable
    @Deprecated(forRemoval = true, since = "10.7.11")
    <ATTRIBUTE extends ChemicalAttribute> ATTRIBUTE getLegacy(Class<ATTRIBUTE> type);

    /**
     * Gets all attribute instances associated with this chemical type.
     *
     * @return collection of attribute instances.
     *
     * @implNote This method will retrieve all attributes, both the legacy ones, and legacy versions of any modern attributes.
     */
    @Deprecated(forRemoval = true, since = "10.7.11")
    Collection<ChemicalAttribute> getAttributes();

    /**
     * Gets all attribute types associated with this chemical type.
     *
     * @return collection of attribute types.
     *
     * @implNote This method will retrieve all attributes, both the legacy ones, and legacy versions of any modern attributes.
     */
    @Deprecated(forRemoval = true, since = "10.7.11")
    Collection<Class<? extends ChemicalAttribute>> getAttributeTypes();

    /**
     * If an attribute type is present, performs the given action with the value, otherwise does nothing.
     *
     * @param type   The type of the attribute.
     * @param action The action to be performed, if a value is present.
     */
    @Deprecated(forRemoval = true, since = "10.7.11")
    default <ATTRIBUTE extends ChemicalAttribute> void ifAttributePresent(Class<ATTRIBUTE> type, Consumer<? super ATTRIBUTE> action) {
        ATTRIBUTE attribute = get(type);
        if (attribute != null) {
            action.accept(attribute);
        }
    }

    /**
     * Attempts to map an attribute to an int value using the given mapper. If the attribute type is not present this will return {@code 0}.
     *
     * @param type   The type of the attribute to map.
     * @param mapper Mapping function.
     *
     * @return Result of applying the mapping function to the attribute or {@code 0} if the attribute is not present.
     */
    @Deprecated(forRemoval = true, since = "10.7.11")
    default <ATTRIBUTE extends ChemicalAttribute> int mapAttributeToInt(Class<ATTRIBUTE> type, ToIntFunction<? super ATTRIBUTE> mapper) {
        ATTRIBUTE attribute = get(type);
        if (attribute != null) {
            return mapper.applyAsInt(attribute);
        }
        return 0;
    }

    /**
     * Attempts to map an attribute to an int value using the given mapper. If the attribute type is not present this will return {@code 0}.
     *
     * @param type   The type of the attribute to map.
     * @param mapper Mapping function.
     *
     * @return Result of applying the mapping function to the attribute or {@code 0} if the attribute is not present.
     */
    @SuppressWarnings("unchecked")
    @Deprecated(forRemoval = true, since = "10.7.11")
    default <ATTRIBUTE extends ChemicalAttribute> int mapAttributeToInt(Class<ATTRIBUTE> type, ToIntBiFunction<SELF, ? super ATTRIBUTE> mapper) {
        ATTRIBUTE attribute = get(type);
        if (attribute != null) {
            return mapper.applyAsInt((SELF) this, attribute);
        }
        return 0;
    }

    /**
     * Attempts to map an attribute to a long value using the given mapper. If the attribute type is not present this will return {@code 0}.
     *
     * @param type   The type of the attribute to map.
     * @param mapper Mapping function.
     *
     * @return Result of applying the mapping function to the attribute or {@code 0} if the attribute is not present.
     */
    @Deprecated(forRemoval = true, since = "10.7.11")
    default <ATTRIBUTE extends ChemicalAttribute> long mapAttributeToLong(Class<ATTRIBUTE> type, ToLongFunction<? super ATTRIBUTE> mapper) {
        ATTRIBUTE attribute = get(type);
        if (attribute != null) {
            return mapper.applyAsLong(attribute);
        }
        return 0;
    }

    /**
     * Attempts to map an attribute to a long value using the given mapper. If the attribute type is not present this will return {@code 0}.
     *
     * @param type   The type of the attribute to map.
     * @param mapper Mapping function.
     *
     * @return Result of applying the mapping function to the attribute or {@code 0} if the attribute is not present.
     */
    @SuppressWarnings("unchecked")
    @Deprecated(forRemoval = true, since = "10.7.11")
    default <ATTRIBUTE extends ChemicalAttribute> long mapAttributeToLong(Class<ATTRIBUTE> type, ToLongBiFunction<SELF, ? super ATTRIBUTE> mapper) {
        ATTRIBUTE attribute = get(type);
        if (attribute != null) {
            return mapper.applyAsLong((SELF) this, attribute);
        }
        return 0;
    }

    /**
     * Attempts to map an attribute to a double value using the given mapper. If the attribute type is not present this will return {@code 0}.
     *
     * @param type   The type of the attribute to map.
     * @param mapper Mapping function.
     *
     * @return Result of applying the mapping function to the attribute or {@code 0} if the attribute is not present.
     */
    @Deprecated(forRemoval = true, since = "10.7.11")
    default <ATTRIBUTE extends ChemicalAttribute> double mapAttributeToDouble(Class<ATTRIBUTE> type, ToDoubleFunction<? super ATTRIBUTE> mapper) {
        ATTRIBUTE attribute = get(type);
        if (attribute != null) {
            return mapper.applyAsDouble(attribute);
        }
        return 0;
    }

    /**
     * Attempts to map an attribute to a double value using the given mapper. If the attribute type is not present this will return {@code 0}.
     *
     * @param type   The type of the attribute to map.
     * @param mapper Mapping function.
     *
     * @return Result of applying the mapping function to the attribute or {@code 0} if the attribute is not present.
     */
    @SuppressWarnings("unchecked")
    @Deprecated(forRemoval = true, since = "10.7.11")
    default <ATTRIBUTE extends ChemicalAttribute> double mapAttributeToDouble(Class<ATTRIBUTE> type, ToDoubleBiFunction<SELF, ? super ATTRIBUTE> mapper) {
        ATTRIBUTE attribute = get(type);
        if (attribute != null) {
            return mapper.applyAsDouble((SELF) this, attribute);
        }
        return 0;
    }

    /**
     * Attempts to map an attribute using the given mapper. If the attribute type is not present this will return {@code fallback}.
     *
     * @param type   The type of the attribute to map.
     * @param mapper Mapping function.
     *
     * @return Result of applying the mapping function to the attribute or {@code fallback} if the attribute is not present.
     */
    @Deprecated(forRemoval = true, since = "10.7.11")
    default <ATTRIBUTE extends ChemicalAttribute, V> V mapAttribute(Class<ATTRIBUTE> type, Function<? super ATTRIBUTE, ? extends V> mapper, V fallback) {
        ATTRIBUTE attribute = get(type);
        if (attribute != null) {
            return mapper.apply(attribute);
        }
        return fallback;
    }

    /**
     * Attempts to map an attribute using the given mapper. If the attribute type is not present this will return {@code fallback}.
     *
     * @param type   The type of the attribute to map.
     * @param mapper Mapping function.
     *
     * @return Result of applying the mapping function to the attribute or {@code fallback} if the attribute is not present.
     */
    @SuppressWarnings("unchecked")
    @Deprecated(forRemoval = true, since = "10.7.11")
    default <ATTRIBUTE extends ChemicalAttribute, V> V mapAttribute(Class<ATTRIBUTE> type, BiFunction<SELF, ? super ATTRIBUTE, ? extends V> mapper, V fallback) {
        ATTRIBUTE attribute = get(type);
        if (attribute != null) {
            return mapper.apply((SELF) this, attribute);
        }
        return fallback;
    }
}