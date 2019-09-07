package mekanism.client.jei.machine.other;

import java.util.Arrays;
import java.util.Collections;
import mekanism.api.recipes.ElectrolysisRecipe;
import mekanism.client.jei.MekanismJEI;
import mekanism.client.jei.machine.MekanismRecipeWrapper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;

public class ElectrolyticSeparatorRecipeWrapper extends MekanismRecipeWrapper<ElectrolysisRecipe> {

    public ElectrolyticSeparatorRecipeWrapper(ElectrolysisRecipe recipe) {
        super(recipe);
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients.setInputLists(VanillaTypes.FLUID, Collections.singletonList(recipe.getInput().getRepresentations()));
        ingredients.setOutputs(MekanismJEI.TYPE_GAS, Arrays.asList(recipe.getLeftGasOutputRepresentation(), recipe.getRightGasOutputRepresentation()));
    }
}