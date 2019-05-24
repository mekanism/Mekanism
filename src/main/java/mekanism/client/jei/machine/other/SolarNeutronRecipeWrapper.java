package mekanism.client.jei.machine.other;

import mekanism.client.jei.MekanismJEI;
import mekanism.client.jei.machine.MekanismRecipeWrapper;
import mekanism.common.recipe.machines.SolarNeutronRecipe;
import mezz.jei.api.ingredients.IIngredients;

public class SolarNeutronRecipeWrapper<RECIPE extends SolarNeutronRecipe> extends MekanismRecipeWrapper<RECIPE> {

    public SolarNeutronRecipeWrapper(RECIPE recipe) {
        super(recipe);
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients.setInput(MekanismJEI.TYPE_GAS, recipe.getInput().ingredient);
        ingredients.setOutput(MekanismJEI.TYPE_GAS, recipe.getOutput().output);
    }
}