package mekanism.client.jei.machine;

import mekanism.common.recipe.machines.BasicMachineRecipe;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;

public class MachineRecipeWrapper<RECIPE extends BasicMachineRecipe<RECIPE>> extends MekanismRecipeWrapper<RECIPE> {

    public MachineRecipeWrapper(RECIPE recipe) {
        super(recipe);
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients.setInput(VanillaTypes.ITEM, recipe.getInput().ingredient);
        ingredients.setOutput(VanillaTypes.ITEM, recipe.getOutput().output);
    }
}