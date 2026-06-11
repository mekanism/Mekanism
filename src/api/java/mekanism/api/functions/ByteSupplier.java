package mekanism.api.functions;

import java.util.function.Supplier;

/// Represents a supplier of `byte`-valued results.  This is the `byte`-producing primitive specialization of [Supplier].
///
/// There is no requirement that a new or distinct result be returned each time the supplier is invoked.
///
/// This is a [functional interface](package-summary.html) whose functional method is [#getAsByte()].
///
/// @see Supplier
@FunctionalInterface
public interface ByteSupplier {

    /// Gets a result.
    ///
    /// @return a result
    byte getAsByte();
}