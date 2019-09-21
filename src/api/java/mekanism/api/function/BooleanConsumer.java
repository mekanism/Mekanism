package mekanism.api.function;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Represents an operation that accepts a single {@code boolean}-valued argument and returns no result.  This is the primitive type specialization of {@link Consumer} for
 * {@code boolean}.  Unlike most other functional interfaces, {@code BooleanConsumer} is expected to operate via side-effects.
 *
 * <p>This is a functional interface whose functional method is {@link #accept(boolean)}.
 *
 * <p>Based on Java's IntConsumer, DoubleConsumer etc
 *
 * @see Consumer
 */
@FunctionalInterface
public interface BooleanConsumer {

    /**
     * Performs this operation on the given argument.
     *
     * @param value the input argument
     */
    void accept(boolean value);

    /**
     * Returns a composed {@code DoubleConsumer} that performs, in sequence, this operation followed by the {@code after} operation. If performing either operation throws
     * an exception, it is relayed to the caller of the composed operation.  If performing this operation throws an exception, the {@code after} operation will not be
     * performed.
     *
     * @param after the operation to perform after this operation
     *
     * @return a composed {@code DoubleConsumer} that performs in sequence this operation followed by the {@code after} operation
     *
     * @throws NullPointerException if {@code after} is null
     */
    default BooleanConsumer andThen(BooleanConsumer after) {
        Objects.requireNonNull(after);
        return t -> {
            accept(t);
            after.accept(t);
        };
    }
}