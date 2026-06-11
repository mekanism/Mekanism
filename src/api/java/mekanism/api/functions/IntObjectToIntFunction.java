package mekanism.api.functions;

import java.util.function.BiFunction;

/// Represents a function that accepts two arguments and produces a int-valued result. This is the `int`-producing primitive specialization for [BiFunction].
///
/// @param <U> the type of the second argument to the function
///
/// @see java.util.function.ToIntBiFunction
/// @since 10.8.0
@FunctionalInterface
public interface IntObjectToIntFunction<U> {

    /// Applies this function to the given arguments.
    ///
    /// @param t the first function argument
    /// @param u the second function argument
    ///
    /// @return the function result
    int applyAsInt(int t, U u);
}
