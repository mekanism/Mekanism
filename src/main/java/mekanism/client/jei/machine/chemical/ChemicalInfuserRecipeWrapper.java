package mekanism.client.jei.machine.chemical;

import java.util.Arrays;
import mekanism.client.jei.MekanismJEI;
import mekanism.common.recipe.machines.ChemicalInfuserRecipe;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;

public class ChemicalInfuserRecipeWrapper implements IRecipeWrapper {

    private final ChemicalInfuserRecipe recipe;

    public ChemicalInfuserRecipeWrapper(ChemicalInfuserRecipe r) {
        recipe = r;
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients
              .setInputs(MekanismJEI.TYPE_GAS, Arrays.asList(recipe.recipeInput.leftGas, recipe.recipeInput.rightGas));
        ingredients.setOutput(MekanismJEI.TYPE_GAS, recipe.recipeOutput.output);
    }

    public ChemicalInfuserRecipe getRecipe() {
        return recipe;
    }
}