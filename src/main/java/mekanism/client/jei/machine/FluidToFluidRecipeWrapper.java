package mekanism.client.jei.machine;

import mekanism.api.recipes.FluidToFluidRecipe;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;

public class FluidToFluidRecipeWrapper extends MekanismRecipeWrapper<FluidToFluidRecipe> {

    public FluidToFluidRecipeWrapper(FluidToFluidRecipe recipe) {
        super(recipe);
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients.setInputs(VanillaTypes.FLUID, recipe.getInput().getRepresentations());
        ingredients.setOutput(VanillaTypes.FLUID, recipe.getOutputRepresentation());
    }
}