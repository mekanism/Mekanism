package mekanism.client.jei.machine;

import java.util.Collections;
import mekanism.api.recipes.ItemStackToItemStackRecipe;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;

public class ItemStackToItemStackRecipeWrapper extends MekanismRecipeWrapper<ItemStackToItemStackRecipe> {

    public ItemStackToItemStackRecipeWrapper(ItemStackToItemStackRecipe recipe) {
        super(recipe);
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients.setInputLists(VanillaTypes.ITEM, Collections.singletonList(recipe.getInput().getRepresentations()));
        ingredients.setOutputLists(VanillaTypes.ITEM, Collections.singletonList(recipe.getOutputDefinition()));
    }
}