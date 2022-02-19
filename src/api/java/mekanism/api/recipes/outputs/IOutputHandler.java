package mekanism.api.recipes.outputs;

/**
 * Interface describing handling of an output.
 *
 * @param <OUTPUT> Type of output handled by this handler.
 */
public interface IOutputHandler<OUTPUT> {

    /**
     * Adds {@code operations} operations worth of {@code toOutput} to the output.
     *
     * @param toOutput   Output result.
     * @param operations Operations to perform.
     */
    void handleOutput(OUTPUT toOutput, int operations);

    /**
     * Calculates how many operations the output has room for.
     *
     * @param toOutput   Output result.
     * @param currentMax The current maximum number of operations that can happen.
     *
     * @return The number of operations the output has room for.
     */
    int operationsRoomFor(OUTPUT toOutput, int currentMax);
}