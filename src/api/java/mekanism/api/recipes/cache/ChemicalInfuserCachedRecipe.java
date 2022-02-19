package mekanism.api.recipes.cache;

import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.ChemicalInfuserRecipe;
import mekanism.api.recipes.cache.chemical.ChemicalChemicalToChemicalCachedRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.inputs.chemical.GasStackIngredient;
import mekanism.api.recipes.outputs.IOutputHandler;

/**
 * Base class to help implement handling of chemical infusing recipes.
 */
@Deprecated//TODO - 1.18: Remove this
@ParametersAreNonnullByDefault
public class ChemicalInfuserCachedRecipe extends ChemicalChemicalToChemicalCachedRecipe<Gas, GasStack, GasStackIngredient, ChemicalInfuserRecipe> {

    /**
     * @param recipe            Recipe.
     * @param leftInputHandler  Left input handler.
     * @param rightInputHandler Right input handler.
     * @param outputHandler     Output handler.
     */
    public ChemicalInfuserCachedRecipe(ChemicalInfuserRecipe recipe, IInputHandler<@NonNull GasStack> leftInputHandler, IInputHandler<@NonNull GasStack> rightInputHandler,
          IOutputHandler<@NonNull GasStack> outputHandler) {
        super(recipe, leftInputHandler, rightInputHandler, outputHandler);
    }
}