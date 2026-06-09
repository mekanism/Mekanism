package mekanism.api.functions;

import java.util.function.BiPredicate;
import java.util.function.BooleanSupplier;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Predicate;
import mekanism.api.AutomationType;
import org.jspecify.annotations.Nullable;

/**
 * Helper class to reduce having to create duplicate objects for constant predicates.
 */
@SuppressWarnings("unchecked")
public class ConstantPredicates {

    private ConstantPredicates() {
    }

    /**
     * A boolean supplier that returns {@code true}.
     *
     * @since 10.5.0
     */
    public static final BooleanSupplier ALWAYS_TRUE = () -> true;

    /**
     * A supplier that returns {@code 0L}.
     *
     * @since 10.6.6
     */
    public static final LongSupplier ZERO_LONG = () -> 0;
    /**
     * A supplier that returns {@code 0}.
     *
     * @since 10.8.0
     */
    public static final IntSupplier ZERO = () -> 0;

    private static final Predicate<Object> alwaysTrue = _ -> true;
    private static final BiPredicate<Object, Object> alwaysTrueBi = (_, _) -> true;

    private static final Predicate<Object> alwaysFalse = _ -> false;
    private static final BiPredicate<Object, Object> alwaysFalseBi = (_, _) -> false;

    private static final BiPredicate<Object, AutomationType> internalOnly = (_, automationType) -> automationType.isInternal();
    private static final BiPredicate<Object, AutomationType> notExternal = (_, automationType) -> !automationType.isExternal();
    private static final BiPredicate<Object, AutomationType> manualOnly = (_, automationType) -> automationType.isManual();

    /**
     * Returns a predicate that returns {@code true} for any input.
     */
    public static <T extends @Nullable Object> Predicate<T> alwaysTrue() {
        return (Predicate<T>) alwaysTrue;
    }

    /**
     * Returns a bi predicate that returns {@code true} for any input.
     */
    public static <T extends @Nullable Object, U extends @Nullable Object> BiPredicate<T, U> alwaysTrueBi() {
        return (BiPredicate<T, U>) alwaysTrueBi;
    }

    /**
     * Returns a predicate that returns {@code false} for any input.
     */
    public static <T extends @Nullable Object> Predicate<T> alwaysFalse() {
        return (Predicate<T>) alwaysFalse;
    }

    /**
     * Returns a bi predicate that returns {@code false} for any input.
     */
    public static <T extends @Nullable Object, V extends @Nullable Object> BiPredicate<T, V> alwaysFalseBi() {
        return (BiPredicate<T, V>) alwaysFalseBi;
    }

    /**
     * Returns a bi predicate that returns {@code true} for any input when the automation type is internal.
     *
     * @since 10.3.3
     */
    public static <T extends @Nullable Object> BiPredicate<T, AutomationType> internalOnly() {
        return (BiPredicate<T, AutomationType>) internalOnly;
    }

    /**
     * Returns a bi predicate that returns {@code true} for any input when the automation type is not external.
     *
     * @since 10.3.3
     */
    public static <T extends @Nullable Object> BiPredicate<T, AutomationType> notExternal() {
        return (BiPredicate<T, AutomationType>) notExternal;
    }

    /**
     * Returns a bi predicate that returns {@code true} for any input when the automation type is manual.
     *
     * @since 10.7.0
     */
    public static <T extends @Nullable Object> BiPredicate<T, AutomationType> manualOnly() {
        return (BiPredicate<T, AutomationType>) manualOnly;
    }

}