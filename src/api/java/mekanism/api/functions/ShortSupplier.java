package mekanism.api.functions;

import java.util.function.Supplier;

/// Represents a supplier of `short`-valued results.  This is the `short`-producing primitive specialization of [Supplier].
///
/// There is no requirement that a new or distinct result be returned each time the supplier is invoked.
///
/// This is a [functional interface](package-summary.html) whose functional method is [#getAsShort()].
///
/// @see Supplier
@FunctionalInterface
public interface ShortSupplier {

    /// Gets a result.
    ///
    /// @return a result
    short getAsShort();
}