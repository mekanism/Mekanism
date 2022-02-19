package mekanism.api.recipes.cache;

import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.GasToGasRecipe;
import mekanism.api.recipes.cache.chemical.ChemicalToChemicalCachedRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.inputs.chemical.GasStackIngredient;
import mekanism.api.recipes.outputs.IOutputHandler;

/**
 * Base class to help implement handling of gas to gas recipes.
 */
@Deprecated//TODO - 1.18: Remove this
@ParametersAreNonnullByDefault
public class GasToGasCachedRecipe extends ChemicalToChemicalCachedRecipe<Gas, GasStack, GasStackIngredient, GasToGasRecipe> {

    /**
     * @param recipe        Recipe.
     * @param inputHandler  Input handler.
     * @param outputHandler Output handler.
     */
    public GasToGasCachedRecipe(GasToGasRecipe recipe, IInputHandler<@NonNull GasStack> inputHandler, IOutputHandler<@NonNull GasStack> outputHandler) {
        super(recipe, inputHandler, outputHandler);
    }
}