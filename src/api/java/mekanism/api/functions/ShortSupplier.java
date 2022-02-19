package mekanism.api.functions;

import java.util.function.Supplier;

/**
 * Represents a supplier of {@code short}-valued results.  This is the {@code short}-producing primitive specialization of {@link Supplier}.
 *
 * <p>There is no requirement that a new or distinct result be returned each time the supplier is invoked.
 *
 * <p>This is a <a href="package-summary.html">functional interface</a> whose functional method is {@link #getAsShort()}.
 *
 * @see Supplier
 */
@FunctionalInterface
public interface ShortSupplier {

    /**
     * Gets a result.
     *
     * @return a result
     */
    short getAsShort();
}