package mekanism.client.jei.machine.chemical;

import mekanism.client.jei.MekanismJEI;
import mekanism.client.jei.machine.MekanismRecipeWrapper;
import mekanism.common.recipe.machines.CrystallizerRecipe;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;

public class ChemicalCrystallizerRecipeWrapper<RECIPE extends CrystallizerRecipe> extends MekanismRecipeWrapper<RECIPE> {

    public ChemicalCrystallizerRecipeWrapper(RECIPE recipe) {
        super(recipe);
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients.setInput(MekanismJEI.TYPE_GAS, recipe.recipeInput.ingredient);
        ingredients.setOutput(VanillaTypes.ITEM, recipe.recipeOutput.output);
    }
}