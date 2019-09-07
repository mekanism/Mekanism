package mekanism.client.jei.machine;

import java.util.Arrays;
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
        ingredients.setInputs(VanillaTypes.ITEM, Arrays.asList(recipe.getInput().getMatchingStacks()));
        ingredients.setOutputLists(VanillaTypes.ITEM, Collections.singletonList(recipe.getOutputDefinition()));
    }
}