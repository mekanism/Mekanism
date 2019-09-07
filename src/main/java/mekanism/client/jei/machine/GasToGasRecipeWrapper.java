package mekanism.client.jei.machine;

import mekanism.api.recipes.GasToGasRecipe;
import mekanism.client.jei.MekanismJEI;
import mezz.jei.api.ingredients.IIngredients;

public class GasToGasRecipeWrapper extends MekanismRecipeWrapper<GasToGasRecipe> {

    public GasToGasRecipeWrapper(GasToGasRecipe recipe) {
        super(recipe);
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients.setInputs(MekanismJEI.TYPE_GAS, recipe.getInput().getRepresentations());
        ingredients.setOutput(MekanismJEI.TYPE_GAS, recipe.getOutputRepresentation());
    }
}