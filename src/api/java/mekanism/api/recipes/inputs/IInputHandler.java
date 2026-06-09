package mekanism.api.recipes.inputs;

import mekanism.api.recipes.cache.CachedRecipe.OperationTracker;
import mekanism.api.recipes.ingredients.InputIngredient;
import net.minecraft.core.TypedInstance;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Range;
import org.jspecify.annotations.Nullable;

/**
 * Interface describing handling of an input.
 *
 * @param <HOLDER_TYPE> Type of input handled by this handler.
 * @param <STACK>       Stack Type of HOLDERTYPE.
 */
public interface IInputHandler<HOLDER_TYPE, STACK extends TypedInstance<HOLDER_TYPE>> {

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

    /// Helper method to check if a given instance of the input is empty.
    ///
    /// @since 10.8.0
    boolean isEmpty(STACK stack);

    /**
     * Gets a copy of the recipe's ingredient that matches the stored input.
     *
     * @param recipeIngredient Recipe ingredient.
     *
     * @return Matching instance. The returned value can be safely modified after.
     */
    STACK getRecipeInput(InputIngredient<HOLDER_TYPE, STACK> recipeIngredient);

    /// Adds `operations` operations worth of `recipeInput` from the input.
    ///
    /// @param recipeInput Recipe input result.
    /// @param operations  Operations to perform.
    /// @param transaction The transaction that this operation is part of.
    ///
    /// @return If the `recipeInput` is null or empty `false`. Otherwise, `true` if there are no operations to perform, or enough input was used to perform all the
    /// operations.
    @Contract("null, _, _ -> false")
    boolean use(@Nullable STACK recipeInput, @Range(from = 0, to = Integer.MAX_VALUE) int operations, TransactionContext transaction);

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