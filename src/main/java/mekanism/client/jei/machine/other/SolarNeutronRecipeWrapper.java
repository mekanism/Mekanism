package mekanism.client.jei.machine.other;

import mekanism.client.jei.MekanismJEI;
import mekanism.common.recipe.machines.SolarNeutronRecipe;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;

public class SolarNeutronRecipeWrapper implements IRecipeWrapper {

    private final SolarNeutronRecipe recipe;

    public SolarNeutronRecipeWrapper(SolarNeutronRecipe r) {
        recipe = r;
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients.setInput(MekanismJEI.GAS_INGREDIENT_TYPE, recipe.getInput().ingredient);
        ingredients.setOutput(MekanismJEI.GAS_INGREDIENT_TYPE, recipe.getOutput().output);
    }

    public SolarNeutronRecipe getRecipe() {
        return recipe;
    }
}
