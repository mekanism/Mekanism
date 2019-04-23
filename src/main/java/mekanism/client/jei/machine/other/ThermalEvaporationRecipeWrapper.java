package mekanism.client.jei.machine.other;

import mekanism.common.recipe.machines.ThermalEvaporationRecipe;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;

public class ThermalEvaporationRecipeWrapper implements IRecipeWrapper {

    private final ThermalEvaporationRecipe recipe;

    public ThermalEvaporationRecipeWrapper(ThermalEvaporationRecipe r) {
        recipe = r;
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients.setInput(VanillaTypes.FLUID, recipe.getInput().ingredient);
        ingredients.setOutput(VanillaTypes.FLUID, recipe.getOutput().output);
    }

    public ThermalEvaporationRecipe getRecipe() {
        return recipe;
    }
}