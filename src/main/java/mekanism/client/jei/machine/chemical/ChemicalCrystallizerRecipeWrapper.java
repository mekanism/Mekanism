package mekanism.client.jei.machine.chemical;

import java.util.Collections;
import mekanism.api.recipes.ChemicalCrystallizerRecipe;
import mekanism.client.jei.MekanismJEI;
import mekanism.client.jei.machine.MekanismRecipeWrapper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;

public class ChemicalCrystallizerRecipeWrapper extends MekanismRecipeWrapper<ChemicalCrystallizerRecipe> {

    public ChemicalCrystallizerRecipeWrapper(ChemicalCrystallizerRecipe recipe) {
        super(recipe);
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients.setInputLists(MekanismJEI.TYPE_GAS, Collections.singletonList(recipe.getInput().getRepresentations()));
        ingredients.setOutputLists(VanillaTypes.ITEM, Collections.singletonList(recipe.getOutputDefinition()));
    }
}