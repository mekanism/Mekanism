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

@ParametersAreNonnullByDefault
public class BoxedChemicalInputHandler {

    private final MergedChemicalTank chemicalTank;

    public BoxedChemicalInputHandler(MergedChemicalTank chemicalTank) {
        this.chemicalTank = Objects.requireNonNull(chemicalTank);
    }

    public BoxedChemicalStack getInput() {
        Current current = chemicalTank.getCurrent();
        if (current == Current.EMPTY) {
            return BoxedChemicalStack.EMPTY;
        }
        return BoxedChemicalStack.box(chemicalTank.getTankFromCurrent(current).getStack());
    }

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

    public void use(BoxedChemicalStack recipeInput, long operations) {
        if (operations == 0) {
            //Just exit if we are somehow here at zero operations
            return;
        }
        if (recipeInput.isEmpty()) {
            //Something went wrong, this if should never really be true if we got to finishProcessing
            return;
        }
        BoxedChemicalStack inputGas = getInput();
        if (!inputGas.isEmpty()) {
            long amount = recipeInput.getChemicalStack().getAmount() * operations;
            logMismatchedStackSize(chemicalTank.getTankForType(inputGas.getChemicalType()).shrinkStack(amount, Action.EXECUTE), amount);
        }
    }

    public int operationsCanSupport(IChemicalStackIngredient<?, ?> recipeIngredient, int currentMax) {
        return operationsCanSupport(recipeIngredient, currentMax, 1);
    }

    public int operationsCanSupport(IChemicalStackIngredient<?, ?> recipeIngredient, int currentMax, long usageMultiplier) {
        if (currentMax <= 0 || usageMultiplier == 0) {
            //Short circuit that if we already can't perform any operations or don't want to use any, just return
            return currentMax;
        }
        BoxedChemicalStack recipeInput = getRecipeInput(recipeIngredient);
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