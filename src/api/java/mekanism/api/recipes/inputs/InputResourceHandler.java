package mekanism.api.recipes.inputs;

import java.util.Objects;
import mekanism.api.AutomationType;
import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.container.IResourceContainer;
import mekanism.api.recipes.cache.CachedRecipe.OperationTracker;
import mekanism.api.recipes.cache.CachedRecipe.OperationTracker.RecipeError;
import mekanism.api.recipes.ingredients.InputIngredient;
import net.minecraft.core.TypedInstance;
import net.neoforged.neoforge.transfer.resource.RegisteredResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

@NothingNullByDefault//TODO - 26.1: Do we want to make this public and add docs?
abstract class InputResourceHandler<HOLDER_TYPE, RESOURCE extends RegisteredResource<HOLDER_TYPE>, CONTAINER extends IResourceContainer<RESOURCE>,
      STACK extends TypedInstance<HOLDER_TYPE>> implements IInputHandler<HOLDER_TYPE, STACK> {

    private final RecipeError notEnoughError;
    protected final CONTAINER container;

    protected InputResourceHandler(CONTAINER container, RecipeError notEnoughError) {
        this.container = Objects.requireNonNull(container, "Container cannot be null.");
        this.notEnoughError = Objects.requireNonNull(notEnoughError, "Not enough input error cannot be null.");
    }

    protected abstract STACK getEmptyStack();

    protected abstract boolean isEmpty(STACK stack);

    protected abstract int getAmount(STACK stack);

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
    public void use(STACK recipeInput, int operations, TransactionContext transaction) {
        if (operations == 0 || isEmpty(recipeInput)) {
            //Just exit if we are somehow here at zero operations
            // or if something went wrong, this if should never really be true if we got to finishProcessing
            return;
        }
        //TODO - 26.1: Why did input tanks check the current stack isn't empty instead of the recipe input not being empty?
        // I am guessing that they are theoretically the same "type" if we get to here so that is why, but it still seems like it was wrong
        if (!isEmpty(recipeInput)) {
            //TODO - 26.1: Protect against overflow by adding a MathUtils#multiplyClamped for ints?
            int amount = getAmount(recipeInput) * operations;
            int extracted = container.extract(asResource(recipeInput), amount, transaction, AutomationType.INTERNAL);
            //TODO - 26.1: We probably should abort if this fails to extract what we expect instead of just logging a warning
            if (amount != extracted) {
                MekanismAPI.logger.error("Stack size changed by a different amount ({}) than requested ({}).", extracted, amount, new Exception());
            }
        }
    }

    @Override
    public void calculateOperationsCanSupport(OperationTracker tracker, STACK recipeInput, int usageMultiplier) {
        //Only calculate if we need to use anything
        if (usageMultiplier > 0) {
            //Test to make sure we can even perform a single operation. This is akin to !recipe.test(inputFluid)
            // Note: If we can't, we treat it as we just don't have enough of the input to better support cases
            // where we may want to allow not having the input be required for recipe matching
            if (!isEmpty(recipeInput)) {
                //TODO - 26.1: Simulate the drain?
                int operations = container.amount() / (getAmount(recipeInput) * usageMultiplier);
                if (operations > 0) {
                    tracker.updateOperations(operations);
                    return;
                }
            }
            // Not enough input to match the recipe, reset the progress
            resetProgress(tracker);
        }
    }

    protected void resetProgress(OperationTracker tracker) {
        tracker.resetProgress(notEnoughError);
    }
}