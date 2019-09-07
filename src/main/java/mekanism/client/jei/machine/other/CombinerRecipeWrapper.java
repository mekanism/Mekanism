package mekanism.client.jei.machine.other;

import java.util.Arrays;
import java.util.Collections;
import mekanism.api.recipes.CombinerRecipe;
import mekanism.client.jei.machine.MekanismRecipeWrapper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;

public class CombinerRecipeWrapper extends MekanismRecipeWrapper<CombinerRecipe> {

    public CombinerRecipeWrapper(CombinerRecipe recipe) {
        super(recipe);
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients.setInputLists(VanillaTypes.ITEM, Arrays.asList(Arrays.asList(recipe.getMainInput().getMatchingStacks()), Arrays.asList(recipe.getExtraInput().getMatchingStacks())));
        ingredients.setOutputLists(VanillaTypes.ITEM, Collections.singletonList(recipe.getOutputDefinition()));
    }
}