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
import mekanism.api.recipes.inputs.chemical.GasStackIngredient;
import mekanism.api.recipes.inputs.chemical.IChemicalStackIngredient;
import mekanism.api.recipes.inputs.chemical.InfusionStackIngredient;
import mekanism.api.recipes.inputs.chemical.PigmentStackIngredient;
import mekanism.api.recipes.inputs.chemical.SlurryStackIngredient;

/**
 * Specialized version of {@link ILongInputHandler} for handling boxed chemicals.
 */
@ParametersAreNonnullByDefault
public class BoxedChemicalInputHandler {

    private final MergedChemicalTank chemicalTank;

    public BoxedChemicalInputHandler(MergedChemicalTank chemicalTank) {
        this.chemicalTank = Objects.requireNonNull(chemicalTank);
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
    public BoxedChemicalStack getRecipeInput(IChemicalStackIngredient<?, ?> recipeIngredient) {
        BoxedChemicalStack input = getInput();
        if (input.isEmpty()) {
            //All recipes currently require that we have an input. If we don't then return that we failed
            return BoxedChemicalStack.EMPTY;
        }
        if (recipeIngredient instanceof GasStackIngredient) {
            if (input.getChemicalType() == ChemicalType.GAS) {
                return BoxedChemicalStack.box(((GasStackIngredient) recipeIngredient).getMatchingInstance((GasStack) input.getChemicalStack()));
            }
        } else if (recipeIngredient instanceof InfusionStackIngredient) {
            if (input.getChemicalType() == ChemicalType.INFUSION) {
                return BoxedChemicalStack.box(((InfusionStackIngredient) recipeIngredient).getMatchingInstance((InfusionStack) input.getChemicalStack()));
            }
        } else if (recipeIngredient instanceof PigmentStackIngredient) {
            if (input.getChemicalType() == ChemicalType.PIGMENT) {
                return BoxedChemicalStack.box(((PigmentStackIngredient) recipeIngredient).getMatchingInstance((PigmentStack) input.getChemicalStack()));
            }
        } else if (recipeIngredient instanceof SlurryStackIngredient) {
            if (input.getChemicalType() == ChemicalType.SLURRY) {
                return BoxedChemicalStack.box(((SlurryStackIngredient) recipeIngredient).getMatchingInstance((SlurryStack) input.getChemicalStack()));
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
     * Calculates how many operations the input can sustain.
     *
     * @param recipeIngredient Recipe ingredient.
     * @param currentMax       The current maximum number of operations that can happen.
     *
     * @return The number of operations the input can sustain.
     */
    @Deprecated//TODO - 1.18: Remove this
    public int operationsCanSupport(IChemicalStackIngredient<?, ?> recipeIngredient, int currentMax) {
        return operationsCanSupport(recipeIngredient, currentMax, 1);
    }

    /**
     * Calculates how many operations the input can sustain.
     *
     * @param recipeIngredient Recipe ingredient.
     * @param currentMax       The current maximum number of operations that can happen.
     * @param usageMultiplier  Usage multiplier to multiply the recipeIngredient's amount by per operation.
     *
     * @return The number of operations the input can sustain.
     */
    @Deprecated//TODO - 1.18: Remove this
    public int operationsCanSupport(IChemicalStackIngredient<?, ?> recipeIngredient, int currentMax, long usageMultiplier) {
        return operationsCanSupport(getRecipeInput(recipeIngredient), currentMax, usageMultiplier);
    }

    /**
     * Calculates how many operations the input can sustain.
     *
     * @param recipeInput Recipe input gotten from {@link #getRecipeInput(IChemicalStackIngredient)}.
     * @param currentMax  The current maximum number of operations that can happen.
     *
     * @return The number of operations the input can sustain.
     */
    public int operationsCanSupport(BoxedChemicalStack recipeInput, int currentMax) {
        return operationsCanSupport(recipeInput, currentMax, 1);
    }

    /**
     * Calculates how many operations the input can sustain.
     *
     * @param recipeInput     Recipe input gotten from {@link #getRecipeInput(IChemicalStackIngredient)}.
     * @param currentMax      The current maximum number of operations that can happen.
     * @param usageMultiplier Usage multiplier to multiply the recipeInput's amount by per operation.
     *
     * @return The number of operations the input can sustain.
     */
    public int operationsCanSupport(BoxedChemicalStack recipeInput, int currentMax, long usageMultiplier) {
        if (currentMax <= 0 || usageMultiplier == 0) {
            //Short circuit that if we already can't perform any operations or don't want to use any, just return
            return currentMax;
        }
        //Test to make sure we can even perform a single operation. This is akin to !recipe.test(inputGas)
        if (recipeInput.isEmpty()) {
            //If the input is empty that means there is no ingredient that matches
            return 0;
        }
        //TODO: Simulate the drain?
        return Math.min(MathUtils.clampToInt(getInput().getChemicalStack().getAmount() / (recipeInput.getChemicalStack().getAmount() * usageMultiplier)), currentMax);
    }

    private static void logMismatchedStackSize(long actual, long expected) {
        if (expected != actual) {
            MekanismAPI.logger.error("Stack size changed by a different amount ({}) than requested ({}).", actual, expected, new Exception());
        }
    }
}