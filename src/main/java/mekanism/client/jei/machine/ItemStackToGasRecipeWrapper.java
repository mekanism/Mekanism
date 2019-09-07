package mekanism.client.jei.machine;

import java.util.Arrays;
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
        ingredients.setInputs(VanillaTypes.ITEM, Arrays.asList(recipe.getInput().getMatchingStacks()));
        ingredients.setOutput(MekanismJEI.TYPE_GAS, recipe.getOutputDefinition());
    }
}