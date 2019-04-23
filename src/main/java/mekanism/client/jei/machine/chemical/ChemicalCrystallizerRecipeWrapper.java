package mekanism.client.jei.machine.chemical;

import mekanism.client.jei.MekanismJEI;
import mekanism.common.recipe.machines.CrystallizerRecipe;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;

public class ChemicalCrystallizerRecipeWrapper implements IRecipeWrapper {

    private final CrystallizerRecipe recipe;

    public ChemicalCrystallizerRecipeWrapper(CrystallizerRecipe r) {
        recipe = r;
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients.setInput(MekanismJEI.TYPE_GAS, recipe.recipeInput.ingredient);
        ingredients.setOutput(VanillaTypes.ITEM, recipe.recipeOutput.output);
    }

    public CrystallizerRecipe getRecipe() {
        return recipe;
    }
}