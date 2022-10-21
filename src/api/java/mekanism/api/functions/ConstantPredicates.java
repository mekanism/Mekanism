package mekanism.api.functions;

import java.util.function.BiPredicate;
import java.util.function.Predicate;
import mekanism.api.AutomationType;
import net.minecraftforge.common.util.TriPredicate;
import org.jetbrains.annotations.NotNull;

/**
 * Helper class to reduce having to create duplicate objects for constant predicates.
 */
@SuppressWarnings("unchecked")
public class ConstantPredicates {

    private ConstantPredicates() {
    }

    private static final Predicate<Object> alwaysTrue = t -> true;
    private static final BiPredicate<Object, Object> alwaysTrueBi = (t, u) -> true;
    private static final TriPredicate<Object, Object, Object> alwaysTrueTri = (t, u, v) -> true;

    private static final Predicate<Object> alwaysFalse = t -> false;
    private static final BiPredicate<Object, Object> alwaysFalseBi = (t, u) -> false;
    private static final TriPredicate<Object, Object, Object> alwaysFalseTri = (t, u, v) -> false;

    private static final BiPredicate<Object, @NotNull AutomationType> internalOnly = (t, automationType) -> automationType == AutomationType.INTERNAL;
    private static final BiPredicate<Object, @NotNull AutomationType> notExternal = (t, automationType) -> automationType != AutomationType.EXTERNAL;

    /**
     * Returns a predicate that returns {@code true} for any input.
     */
    public static <T> Predicate<T> alwaysTrue() {
        return (Predicate<T>) alwaysTrue;
    }

    /**
     * Returns a bi predicate that returns {@code true} for any input.
     */
    public static <T, U> BiPredicate<T, U> alwaysTrueBi() {
        return (BiPredicate<T, U>) alwaysTrueBi;
    }

    /**
     * Returns a tri predicate that returns {@code true} for any input.
     */
    public static <T, U, V> TriPredicate<T, U, V> alwaysTrueTri() {
        return (TriPredicate<T, U, V>) alwaysTrueTri;
    }

    /**
     * Returns a predicate that returns {@code false} for any input.
     */
    public static <T> Predicate<T> alwaysFalse() {
        return (Predicate<T>) alwaysFalse;
    }

    /**
     * Returns a bi predicate that returns {@code false} for any input.
     */
    public static <T, V> BiPredicate<T, V> alwaysFalseBi() {
        return (BiPredicate<T, V>) alwaysFalseBi;
    }

    /**
     * Returns a tri predicate that returns {@code false} for any input.
     */
    public static <T, U, V> TriPredicate<T, U, V> alwaysFalseTri() {
        return (TriPredicate<T, U, V>) alwaysFalseTri;
    }

    /**
     * Returns a bi predicate that returns {@code true} for any input when the automation type is internal.
     *
     * @since 10.3.3
     */
    public static <T> BiPredicate<T, @NotNull AutomationType> internalOnly() {
        return (BiPredicate<T, @NotNull AutomationType>) internalOnly;
    }

    /**
     * Returns a bi predicate that returns {@code true} for any input when the automation type is not external.
     *
     * @since 10.3.3
     */
    public static <T> BiPredicate<T, @NotNull AutomationType> notExternal() {
        return (BiPredicate<T, @NotNull AutomationType>) notExternal;
    }
}