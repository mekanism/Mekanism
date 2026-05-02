package mekanism.api.recipes.inputs;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.recipes.cache.CachedRecipe.OperationTracker;
import mekanism.api.recipes.ingredients.InputIngredient;

/**
 * Interface describing handling of an input that can handle long values.
 *
 * @param <INPUT> Type of input handled by this handler.
 */
@NothingNullByDefault
public interface ILongInputHandler<INPUT> extends IInputHandler<INPUT> {

    @Override
    default void use(INPUT recipeInput, int operations) {
        //Wrap to the long implementation
        use(recipeInput, (long) operations);
    }

    /**
     * Adds {@code operations} operations worth of {@code recipeInput} from the input.
     *
     * @param recipeInput Recipe input result.
     * @param operations  Operations to perform.
     */
    void use(INPUT recipeInput, long operations);

    @Override
    default void calculateOperationsCanSupport(OperationTracker tracker, INPUT recipeInput, int usageMultiplier) {
        //Wrap to the long implementation
        calculateOperationsCanSupport(tracker, recipeInput, (long) usageMultiplier);
    }

    /**
     * Calculates how many operations the input can sustain and updates the given operation tracker. It can be assumed that when this method is called
     * {@link OperationTracker#shouldContinueChecking()} is {@code true}.
     *
     * @param tracker         Tracker of current errors and max operations.
     * @param recipeInput     Recipe input gotten from {@link #getRecipeInput(InputIngredient)}.
     * @param usageMultiplier Usage multiplier to multiply the recipeInput's amount by per operation.
     */
    void calculateOperationsCanSupport(OperationTracker tracker, INPUT recipeInput, long usageMultiplier);
}