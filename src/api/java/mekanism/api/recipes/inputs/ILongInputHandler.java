package mekanism.api.recipes.inputs;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.recipes.cache.CachedRecipe.OperationTracker;
import mekanism.api.recipes.ingredients.InputIngredient;
import net.minecraft.core.TypedInstance;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * Interface describing handling of an input that can handle long values.
 *
 * @param <STACK> Type of input handled by this handler.
 */
@NothingNullByDefault
public interface ILongInputHandler<HOLDERTYPE, STACK extends TypedInstance<HOLDERTYPE>> extends IInputHandler<HOLDERTYPE, STACK> {

    @Override
    default void use(STACK recipeInput, int operations, TransactionContext transaction) {
        //Wrap to the long implementation
        use(recipeInput, (long) operations, transaction);
    }

    /**
     * Adds {@code operations} operations worth of {@code recipeInput} from the input.
     *
     * @param recipeInput Recipe input result.
     * @param operations  Operations to perform.
     * @param transaction The transaction that this operation is part of.
     */
    void use(STACK recipeInput, long operations, TransactionContext transaction);

    @Override
    default void calculateOperationsCanSupport(OperationTracker tracker, STACK recipeInput, int usageMultiplier) {
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
    void calculateOperationsCanSupport(OperationTracker tracker, STACK recipeInput, long usageMultiplier);
}