package mekanism.fabric_shim.common.util;

import java.util.Objects;

/**
 * Three-argument predicate (stand-in for NeoForge's TriPredicate; same surface).
 */
@FunctionalInterface
public interface TriPredicate<T, U, V> {

    boolean test(T t, U u, V v);

    default TriPredicate<T, U, V> and(TriPredicate<? super T, ? super U, ? super V> other) {
        Objects.requireNonNull(other);
        return (t, u, v) -> test(t, u, v) && other.test(t, u, v);
    }

    default TriPredicate<T, U, V> negate() {
        return (t, u, v) -> !test(t, u, v);
    }

    default TriPredicate<T, U, V> or(TriPredicate<? super T, ? super U, ? super V> other) {
        Objects.requireNonNull(other);
        return (t, u, v) -> test(t, u, v) || other.test(t, u, v);
    }
}
