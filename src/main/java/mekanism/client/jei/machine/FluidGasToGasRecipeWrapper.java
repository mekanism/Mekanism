package mekanism.client.jei.machine;

import java.util.Collections;
import mekanism.api.recipes.FluidGasToGasRecipe;
import mekanism.client.jei.MekanismJEI;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;

public class FluidGasToGasRecipeWrapper extends MekanismRecipeWrapper<FluidGasToGasRecipe> {

    public FluidGasToGasRecipeWrapper(FluidGasToGasRecipe recipe) {
        super(recipe);
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients.setInputLists(VanillaTypes.FLUID, Collections.singletonList(recipe.getFluidInput().getRepresentations()));
        ingredients.setInputLists(MekanismJEI.TYPE_GAS, Collections.singletonList(recipe.getGasInput().getRepresentations()));
        ingredients.setOutput(MekanismJEI.TYPE_GAS, recipe.getOutputRepresentation());
    }
}