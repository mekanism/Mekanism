package mekanism.client.jei.machine.chemical;

import java.util.Arrays;
import mekanism.client.jei.MekanismJEI;
import mekanism.client.jei.machine.MekanismRecipeWrapper;
import mekanism.common.recipe.machines.ChemicalInfuserRecipe;
import mezz.jei.api.ingredients.IIngredients;

public class ChemicalInfuserRecipeWrapper<RECIPE extends ChemicalInfuserRecipe> extends MekanismRecipeWrapper<RECIPE> {

    public ChemicalInfuserRecipeWrapper(RECIPE recipe) {
        super(recipe);
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients.setInputs(MekanismJEI.TYPE_GAS, Arrays.asList(recipe.recipeInput.leftGas, recipe.recipeInput.rightGas));
        ingredients.setOutput(MekanismJEI.TYPE_GAS, recipe.recipeOutput.output);
    }
}