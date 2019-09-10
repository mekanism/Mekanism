package mekanism.client.jei.machine.chemical;

import java.util.Collections;
import mekanism.api.recipes.ChemicalWasherRecipe;
import mekanism.client.jei.MekanismJEI;
import mekanism.client.jei.machine.MekanismRecipeWrapper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;

public class ChemicalWasherRecipeWrapper extends MekanismRecipeWrapper<ChemicalWasherRecipe> {

    public ChemicalWasherRecipeWrapper(ChemicalWasherRecipe recipe) {
        super(recipe);
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients.setInputLists(VanillaTypes.FLUID, Collections.singletonList(recipe.getCleansingIngredient().getRepresentations()));
        ingredients.setInputLists(MekanismJEI.TYPE_GAS, Collections.singletonList(recipe.getInput().getRepresentations()));
        ingredients.setOutput(MekanismJEI.TYPE_GAS, recipe.getOutputRepresentation());
    }
}