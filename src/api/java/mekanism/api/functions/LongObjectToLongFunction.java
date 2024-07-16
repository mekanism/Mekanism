package mekanism.api.functions;

import java.util.function.BiFunction;

/**
 * Represents a function that accepts two arguments and produces a long-valued result. This is the {@code long}-producing primitive specialization for
 * {@link BiFunction}.
 *
 * @param <U> the type of the second argument to the function
 *
 * @see java.util.function.ToLongBiFunction
 * @since 10.6.6
 */
@FunctionalInterface
public interface LongObjectToLongFunction<U> {

    /**
     * Applies this function to the given arguments.
     *
     * @param t the first function argument
     * @param u the second function argument
     *
     * @return the function result
     */
    long applyAsLong(long t, U u);
}
