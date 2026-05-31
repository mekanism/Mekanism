package mekanism.api.recipes.inputs;

import java.util.Objects;
import mekanism.api.AutomationType;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.recipes.cache.CachedRecipe.OperationTracker;
import mekanism.api.recipes.cache.CachedRecipe.OperationTracker.RecipeError;
import mekanism.api.recipes.ingredients.InputIngredient;
import mekanism.api.resource.IResourceContainer;
import net.minecraft.core.TypedInstance;
import net.neoforged.neoforge.transfer.resource.RegisteredResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Range;
import org.jspecify.annotations.Nullable;

/// Base helper implementation for [IInputHandler] that simplifies creating input handlers for resource containers.
///
/// @since 10.8.0
@NothingNullByDefault
public abstract class InputResourceHandler<HOLDER_TYPE, RESOURCE extends RegisteredResource<HOLDER_TYPE>, CONTAINER extends IResourceContainer<RESOURCE>,
      STACK extends TypedInstance<HOLDER_TYPE>> implements IInputHandler<HOLDER_TYPE, STACK> {

    private final RecipeError notEnoughError;
    protected final CONTAINER container;

    /// @param container      The input container.
    /// @param notEnoughError The recipe error for when there isn't enough of an input.
    protected InputResourceHandler(CONTAINER container, RecipeError notEnoughError) {
        this.container = Objects.requireNonNull(container, "Container cannot be null.");
        this.notEnoughError = Objects.requireNonNull(notEnoughError, "Not enough input error cannot be null.");
    }

    /// {@return an instance of the empty stack}
    protected abstract STACK getEmptyStack();

    /// {@return the size of the given stack}
    ///
    /// @param stack Stack to represent as a resource.
    protected abstract int getAmount(STACK stack);

    /// {@return the resource type of the given stack}
    ///
    /// @param stack Stack to get the size of.
    protected abstract RESOURCE asResource(STACK stack);

    @Override
    public STACK getRecipeInput(InputIngredient<HOLDER_TYPE, STACK> recipeIngredient) {
        if (container.isEmpty()) {
            //All recipes currently require that we have an input. If we don't then return that we failed
            return getEmptyStack();
        }
        return recipeIngredient.getMatchingInstance(getInput());
    }

    @Override
    @Contract("null, _, _ -> false")
    public boolean use(@Nullable STACK recipeInput, @Range(from = 0, to = Integer.MAX_VALUE) int operations, TransactionContext transaction) {
        if (recipeInput == null || isEmpty(recipeInput)) {
            //If there is no recipe input, something went wrong in calling this method, and return false. In theory this should never happen
            return false;
        } else if (operations == 0) {
            //If we have no operations to perform just return that we used everything we needed to
            return true;
        }
        //Note: We know this shouldn't overflow, as we clamped the operations based on usage in calculateOperationsCanSupport
        int amount = getAmount(recipeInput) * operations;
        return container.extract(asResource(recipeInput), amount, transaction, AutomationType.INTERNAL) == amount;
    }

    @Override
    public void calculateOperationsCanSupport(OperationTracker tracker, STACK recipeInput, int usageMultiplier) {
        //Only calculate if we need to use anything
        if (usageMultiplier > 0) {
            //Test to make sure we can even perform a single operation. This is akin to !recipe.test(inputFluid)
            // Note: If we can't, we treat it as we just don't have enough of the input to better support cases
            // where we may want to allow not having the input be required for recipe matching
            RESOURCE inputType = asResource(recipeInput);
            if (!inputType.isEmpty()) {
                try (Transaction simulation = tracker.openSimulation()) {
                    //Calculate how much we are actually able to extract from the container
                    int available = container.extract(inputType, container.amountAsInt(), simulation, AutomationType.INTERNAL);
                    if (available > 0) {
                        int operations = available / (getAmount(recipeInput) * usageMultiplier);
                        if (operations > 0) {
                            tracker.updateOperations(operations);
                            return;
                        }
                    }
                }
            }
            // Not enough input to match the recipe, reset the progress
            resetProgress(tracker);
        }
    }

    /// Helper method to make it easier to override and create input handlers that just pause instead of fully resetting their progress.
    protected void resetProgress(OperationTracker tracker) {
        tracker.resetProgress(notEnoughError);
    }
}