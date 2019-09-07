package mekanism.client.jei.machine.chemical;

import java.util.Arrays;
import java.util.Collections;
import mekanism.api.recipes.ChemicalInfuserRecipe;
import mekanism.client.jei.MekanismJEI;
import mekanism.client.jei.machine.MekanismRecipeWrapper;
import mezz.jei.api.ingredients.IIngredients;

public class ChemicalInfuserRecipeWrapper extends MekanismRecipeWrapper<ChemicalInfuserRecipe> {

    public ChemicalInfuserRecipeWrapper(ChemicalInfuserRecipe recipe) {
        super(recipe);
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients.setInputLists(MekanismJEI.TYPE_GAS, Arrays.asList(recipe.getLeftInput().getRepresentations(), recipe.getRightInput().getRepresentations()));
        ingredients.setOutputLists(MekanismJEI.TYPE_GAS, Collections.singletonList(recipe.getOutputDefinition()));
    }
}