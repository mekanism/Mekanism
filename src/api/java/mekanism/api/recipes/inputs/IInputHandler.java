package mekanism.api.recipes.inputs;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.recipes.cache.CachedRecipe.OperationTracker;
import mekanism.api.recipes.ingredients.InputIngredient;
import net.minecraft.core.TypedInstance;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * Interface describing handling of an input.
 *
 * @param <HOLDERTYPE> Type of input handled by this handler.
 * @param <STACK>      Stack Type of HOLDERTYPE.
 */
@NothingNullByDefault
public interface IInputHandler<HOLDERTYPE, STACK extends TypedInstance<HOLDERTYPE>> {

    /**
     * Returns the currently stored input.
     *
     * <p>
     * <strong>IMPORTANT:</strong> This input <em>MUST NOT</em> be modified. This method is not for altering an input's contents. Any implementers who
     * are able to detect modification through this method should throw an exception.
     * </p>
     * <p>
     * <strong><em>SERIOUSLY: DO NOT MODIFY THE RETURNED INPUT</em></strong>
     * </p>
     *
     * @return Input stored.
     *
     * @apiNote <strong>IMPORTANT:</strong> Do not modify this value.
     */
    STACK getInput();

    /**
     * Gets a copy of the recipe's ingredient that matches the stored input.
     *
     * @param recipeIngredient Recipe ingredient.
     *
     * @return Matching instance. The returned value can be safely modified after.
     */
    STACK getRecipeInput(InputIngredient<HOLDERTYPE, STACK> recipeIngredient);

    /**
     * Adds {@code operations} operations worth of {@code recipeInput} from the input.
     *
     * @param recipeInput Recipe input result.
     * @param operations  Operations to perform.
     * @param transaction The transaction that this operation is part of.
     */
    void use(STACK recipeInput, int operations, TransactionContext transaction);

    /**
     * Calculates how many operations the input can sustain and updates the given operation tracker. It can be assumed that when this method is called
     * {@link OperationTracker#shouldContinueChecking()} is {@code true}.
     *
     * @param tracker     Tracker of current errors and max operations.
     * @param recipeInput Recipe input gotten from {@link #getRecipeInput(InputIngredient)}.
     */
    default void calculateOperationsCanSupport(OperationTracker tracker, STACK recipeInput) {
        calculateOperationsCanSupport(tracker, recipeInput, 1);
    }

    /**
     * Calculates how many operations the input can sustain and updates the given operation tracker. It can be assumed that when this method is called
     * {@link OperationTracker#shouldContinueChecking()} is {@code true}.
     *
     * @param tracker         Tracker of current errors and max operations.
     * @param recipeInput     Recipe input gotten from {@link #getRecipeInput(InputIngredient)}.
     * @param usageMultiplier Usage multiplier to multiply the recipeInput's amount by per operation.
     */
    void calculateOperationsCanSupport(OperationTracker tracker, STACK recipeInput, int usageMultiplier);
}