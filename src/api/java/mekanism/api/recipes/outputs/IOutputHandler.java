package mekanism.api.recipes.outputs;

import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.recipes.cache.CachedRecipe.OperationTracker;

/**
 * Interface describing handling of an output.
 *
 * @param <OUTPUT> Type of output handled by this handler.
 */
@ParametersAreNotNullByDefault
public interface IOutputHandler<OUTPUT> {

    /**
     * Adds {@code operations} operations worth of {@code toOutput} to the output.
     *
     * @param toOutput   Output result.
     * @param operations Operations to perform.
     */
    void handleOutput(OUTPUT toOutput, int operations);

    /**
     * Calculates how many operations the output has room for and updates the given operation tracker. It can be assumed that when this method is called
     * {@link OperationTracker#shouldContinueChecking()} is {@code true}.
     *
     * @param tracker  Tracker of current errors and max operations.
     * @param toOutput Output result.
     */
    void calculateOperationsCanSupport(OperationTracker tracker, OUTPUT toOutput);
}