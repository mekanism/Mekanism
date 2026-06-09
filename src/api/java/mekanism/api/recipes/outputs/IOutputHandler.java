package mekanism.api.recipes.outputs;

import mekanism.api.recipes.cache.CachedRecipe.OperationTracker;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

/**
 * Interface describing handling of an output.
 *
 * @param <OUTPUT> Type of output handled by this handler.
 */
public interface IOutputHandler<OUTPUT> {

    /// Adds `operations` operations worth of `toOutput` to the output.
    ///
    /// @param toOutput    Output result.
    /// @param operations  Operations to perform.
    /// @param transaction The transaction that this operation is part of.
    ///
    /// @return If the `toOutput` is null or empty `false`. Otherwise, `true` if there are no operations to perform, or the produced output for all the operations was
    /// added.
    @Contract("null, _, _ -> false")
    boolean handleOutput(@Nullable OUTPUT toOutput, int operations, TransactionContext transaction);

    /**
     * Calculates how many operations the output has room for and updates the given operation tracker. It can be assumed that when this method is called
     * {@link OperationTracker#shouldContinueChecking()} is {@code true}.
     *
     * @param tracker  Tracker of current errors and max operations.
     * @param toOutput Output result.
     */
    void calculateOperationsCanSupport(OperationTracker tracker, OUTPUT toOutput);
}