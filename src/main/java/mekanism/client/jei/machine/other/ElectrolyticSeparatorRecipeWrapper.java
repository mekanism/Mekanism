package mekanism.client.jei.machine.other;

import java.util.Arrays;
import mekanism.client.jei.MekanismJEI;
import mekanism.common.recipe.machines.SeparatorRecipe;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;

public class ElectrolyticSeparatorRecipeWrapper implements IRecipeWrapper {

    private final SeparatorRecipe recipe;

    public ElectrolyticSeparatorRecipeWrapper(SeparatorRecipe r) {
        recipe = r;
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients.setInput(VanillaTypes.FLUID, recipe.recipeInput.ingredient);
        ingredients.setOutputs(MekanismJEI.TYPE_GAS,
              Arrays.asList(recipe.recipeOutput.leftGas, recipe.recipeOutput.rightGas));
    }

    public SeparatorRecipe getRecipe() {
        return recipe;
    }
}