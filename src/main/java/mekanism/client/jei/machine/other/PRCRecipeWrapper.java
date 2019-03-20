package mekanism.client.jei.machine.other;

import mekanism.client.jei.MekanismJEI;
import mekanism.common.recipe.machines.PressurizedRecipe;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;

public class PRCRecipeWrapper implements IRecipeWrapper {

    private final PressurizedRecipe recipe;

    public PRCRecipeWrapper(PressurizedRecipe r) {
        recipe = r;
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients.setInput(VanillaTypes.ITEM, recipe.recipeInput.getSolid());
        ingredients.setInput(VanillaTypes.FLUID, recipe.recipeInput.getFluid());
        ingredients.setInput(MekanismJEI.TYPE_GAS, recipe.recipeInput.getGas());
        ingredients.setOutput(VanillaTypes.ITEM, recipe.recipeOutput.getItemOutput());
        ingredients.setOutput(MekanismJEI.TYPE_GAS, recipe.recipeOutput.getGasOutput());
    }

    public PressurizedRecipe getRecipe() {
        return recipe;
    }
}