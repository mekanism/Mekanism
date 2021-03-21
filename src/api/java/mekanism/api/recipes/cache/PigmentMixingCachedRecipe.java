package mekanism.api.recipes.cache;

import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.recipes.PigmentMixingRecipe;
import mekanism.api.recipes.cache.chemical.ChemicalChemicalToChemicalCachedRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.inputs.chemical.PigmentStackIngredient;
import mekanism.api.recipes.outputs.IOutputHandler;

@ParametersAreNonnullByDefault
public class PigmentMixingCachedRecipe extends ChemicalChemicalToChemicalCachedRecipe<Pigment, PigmentStack, PigmentStackIngredient, PigmentMixingRecipe> {

    public PigmentMixingCachedRecipe(PigmentMixingRecipe recipe, IInputHandler<@NonNull PigmentStack> leftInputHandler,
          IInputHandler<@NonNull PigmentStack> rightInputHandler, IOutputHandler<@NonNull PigmentStack> outputHandler) {
        super(recipe, leftInputHandler, rightInputHandler, outputHandler);
    }
}