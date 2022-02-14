package mekanism.api.recipes.inputs;

import java.util.Objects;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.Action;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.ChemicalType;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.merged.BoxedChemicalStack;
import mekanism.api.chemical.merged.MergedChemicalTank;
import mekanism.api.chemical.merged.MergedChemicalTank.Current;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.math.MathUtils;
import mekanism.api.recipes.cache.CachedRecipe.OperationTracker;
import mekanism.api.recipes.cache.CachedRecipe.OperationTracker.RecipeError;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.GasStackIngredient;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.InfusionStackIngredient;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.PigmentStackIngredient;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.SlurryStackIngredient;
import net.minecraft.MethodsReturnNonnullByDefault;

/**
 * Specialized version of {@link ILongInputHandler} for handling boxed chemicals.
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BoxedChemicalInputHandler {

    private final MergedChemicalTank chemicalTank;
    private final RecipeError notEnoughError;

    public BoxedChemicalInputHandler(MergedChemicalTank chemicalTank, RecipeError notEnoughError) {
        this.chemicalTank = Objects.requireNonNull(chemicalTank, "Tank cannot be null.");
        this.notEnoughError = Objects.requireNonNull(notEnoughError, "Not enough input error cannot be null.");
    }

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
    public BoxedChemicalStack getInput() {
        Current current = chemicalTank.getCurrent();
        if (current == Current.EMPTY) {
            return BoxedChemicalStack.EMPTY;
        }
        return BoxedChemicalStack.box(chemicalTank.getTankFromCurrent(current).getStack());
    }

    /**
     * Gets a copy of the recipe's ingredient that matches the stored input.
     *
     * @param recipeIngredient Recipe ingredient.
     *
     * @return Matching instance. The returned value can be safely modified after.
     */
    public BoxedChemicalStack getRecipeInput(ChemicalStackIngredient<?, ?> recipeIngredient) {
        BoxedChemicalStack input = getInput();
        if (input.isEmpty()) {
            //All recipes currently require that we have an input. If we don't then return that we failed
            return BoxedChemicalStack.EMPTY;
        }
        if (recipeIngredient instanceof GasStackIngredient ingredient) {
            if (input.getChemicalType() == ChemicalType.GAS) {
                return BoxedChemicalStack.box(ingredient.getMatchingInstance((GasStack) input.getChemicalStack()));
            }
        } else if (recipeIngredient instanceof InfusionStackIngredient ingredient) {
            if (input.getChemicalType() == ChemicalType.INFUSION) {
                return BoxedChemicalStack.box(ingredient.getMatchingInstance((InfusionStack) input.getChemicalStack()));
            }
        } else if (recipeIngredient instanceof PigmentStackIngredient ingredient) {
            if (input.getChemicalType() == ChemicalType.PIGMENT) {
                return BoxedChemicalStack.box(ingredient.getMatchingInstance((PigmentStack) input.getChemicalStack()));
            }
        } else if (recipeIngredient instanceof SlurryStackIngredient ingredient) {
            if (input.getChemicalType() == ChemicalType.SLURRY) {
                return BoxedChemicalStack.box(ingredient.getMatchingInstance((SlurryStack) input.getChemicalStack()));
            }
        } else {
            throw new IllegalStateException("Unknown Chemical Type");
        }
        //Something went wrong, input doesn't match types with ingredient
        return BoxedChemicalStack.EMPTY;
    }

    /**
     * Adds {@code operations} operations worth of {@code recipeInput} from the input.
     *
     * @param recipeInput Recipe input result.
     * @param operations  Operations to perform.
     */
    public void use(BoxedChemicalStack recipeInput, long operations) {
        if (operations == 0 || recipeInput.isEmpty()) {
            //Just exit if we are somehow here at zero operations
            // or if something went wrong, this if should never really be true if we got to finishProcessing
            return;
        }
        BoxedChemicalStack inputGas = getInput();
        if (!inputGas.isEmpty()) {
            long amount = recipeInput.getChemicalStack().getAmount() * operations;
            logMismatchedStackSize(chemicalTank.getTankForType(inputGas.getChemicalType()).shrinkStack(amount, Action.EXECUTE), amount);
        }
    }

    /**
     * Calculates how many operations the input can sustain and updates the given operation tracker. It can be assumed that when this method is called {@link
     * OperationTracker#shouldContinueChecking()} is {@code true}.
     *
     * @param tracker     Tracker of current errors and max operations.
     * @param recipeInput Recipe input gotten from {@link #getRecipeInput(ChemicalStackIngredient)}.
     */
    public void calculateOperationsCanSupport(OperationTracker tracker, BoxedChemicalStack recipeInput) {
        calculateOperationsCanSupport(tracker, recipeInput, 1);
    }

    /**
     * Calculates how many operations the input can sustain and updates the given operation tracker. It can be assumed that when this method is called {@link
     * OperationTracker#shouldContinueChecking()} is {@code true}.
     *
     * @param tracker         Tracker of current errors and max operations.
     * @param recipeInput     Recipe input gotten from {@link #getRecipeInput(ChemicalStackIngredient)}.
     * @param usageMultiplier Usage multiplier to multiply the recipeInput's amount by per operation.
     */
    public void calculateOperationsCanSupport(OperationTracker tracker, BoxedChemicalStack recipeInput, long usageMultiplier) {
        //Only calculate if we need to use anything
        if (usageMultiplier > 0) {
            //Test to make sure we can even perform a single operation. This is akin to !recipe.test(inputGas)
            if (!recipeInput.isEmpty()) {
                //TODO: Simulate the drain?
                int operations = MathUtils.clampToInt(getInput().getChemicalStack().getAmount() / (recipeInput.getChemicalStack().getAmount() * usageMultiplier));
                if (operations > 0) {
                    tracker.updateOperations(operations);
                    return;
                }
            }
            // Not enough input to match the recipe, reset the progress
            tracker.resetProgress(notEnoughError);
        }
    }

    private static void logMismatchedStackSize(long actual, long expected) {
        if (expected != actual) {
            MekanismAPI.logger.error("Stack size changed by a different amount ({}) than requested ({}).", actual, expected, new Exception());
        }
    }
}