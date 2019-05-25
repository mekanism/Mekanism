package mekanism.client.jei.machine.other;

import mekanism.client.jei.machine.MekanismRecipeWrapper;
import mekanism.common.recipe.machines.ThermalEvaporationRecipe;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;

public class ThermalEvaporationRecipeWrapper<RECIPE extends ThermalEvaporationRecipe> extends MekanismRecipeWrapper<RECIPE> {

    public ThermalEvaporationRecipeWrapper(RECIPE recipe) {
        super(recipe);
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients.setInput(VanillaTypes.FLUID, recipe.getInput().ingredient);
        ingredients.setOutput(VanillaTypes.FLUID, recipe.getOutput().output);
    }
}