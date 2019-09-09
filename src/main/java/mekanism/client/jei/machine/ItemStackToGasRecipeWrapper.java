package mekanism.client.jei.machine;

import java.util.Collections;
import mekanism.api.recipes.ItemStackToGasRecipe;
import mekanism.client.jei.MekanismJEI;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;

public class ItemStackToGasRecipeWrapper extends MekanismRecipeWrapper<ItemStackToGasRecipe> {

    public ItemStackToGasRecipeWrapper(ItemStackToGasRecipe recipe) {
        super(recipe);
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients.setInputLists(VanillaTypes.ITEM, Collections.singletonList(recipe.getInput().getRepresentations()));
        ingredients.setOutput(MekanismJEI.TYPE_GAS, recipe.getOutputDefinition());
    }
}