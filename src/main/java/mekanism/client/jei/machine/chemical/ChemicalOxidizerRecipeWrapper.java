package mekanism.client.jei.machine.chemical;

import mekanism.client.jei.MekanismJEI;
import mekanism.common.recipe.machines.OxidationRecipe;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;

public class ChemicalOxidizerRecipeWrapper implements IRecipeWrapper {

    private final OxidationRecipe recipe;

    public ChemicalOxidizerRecipeWrapper(OxidationRecipe r) {
        recipe = r;
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients.setInput(VanillaTypes.ITEM, recipe.recipeInput.ingredient);
        ingredients.setOutput(MekanismJEI.TYPE_GAS, recipe.recipeOutput.output);
    }

    public OxidationRecipe getRecipe() {
        return recipe;
    }
}