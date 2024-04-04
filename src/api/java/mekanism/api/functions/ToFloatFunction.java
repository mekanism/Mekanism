package mekanism.api.functions;

import java.util.function.Function;

/**
 * Represents a function that produces a float-valued result.  This is the {@code float}-producing primitive specialization for {@link Function}.
 *
 * <p>This is a <a href="package-summary.html">functional interface</a>
 * whose functional method is {@link #applyAsFloat(Object)}.
 *
 * @param <T> the type of the input to the function
 *
 * @see Function
 * @since 10.5.15
 */
@FunctionalInterface
public interface ToFloatFunction<T> {

    /**
     * Applies this function to the given argument.
     *
     * @param value the function argument
     * @return the function result
     */
    float applyAsFloat(T value);
}