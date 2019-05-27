package mekanism.client.jei.machine.other;

import mekanism.client.jei.MekanismJEI;
import mekanism.client.jei.machine.MekanismRecipeWrapper;
import mekanism.common.recipe.machines.PressurizedRecipe;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;

public class PRCRecipeWrapper<RECIPE extends PressurizedRecipe> extends MekanismRecipeWrapper<RECIPE> {

    public PRCRecipeWrapper(RECIPE recipe) {
        super(recipe);
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients.setInput(VanillaTypes.ITEM, recipe.recipeInput.getSolid());
        ingredients.setInput(VanillaTypes.FLUID, recipe.recipeInput.getFluid());
        ingredients.setInput(MekanismJEI.TYPE_GAS, recipe.recipeInput.getGas());
        ingredients.setOutput(VanillaTypes.ITEM, recipe.recipeOutput.getItemOutput());
        ingredients.setOutput(MekanismJEI.TYPE_GAS, recipe.recipeOutput.getGasOutput());
    }
}