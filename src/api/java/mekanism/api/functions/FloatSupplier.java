package mekanism.api.functions;

import java.util.function.Supplier;

/// Represents a supplier of `float`-valued results.  This is the `float`-producing primitive specialization of [Supplier].
///
/// There is no requirement that a new or distinct result be returned each time the supplier is invoked.
///
/// This is a [functional interface](package-summary.html) whose functional method is [#getAsFloat()].
///
/// @see Supplier
@FunctionalInterface
public interface FloatSupplier {

    /// Gets a result.
    ///
    /// @return a result
    float getAsFloat();
}