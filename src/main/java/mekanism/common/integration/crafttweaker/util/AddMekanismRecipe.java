package mekanism.common.integration.crafttweaker.util;

import mekanism.api.recipes.MekanismRecipe;
import mekanism.common.recipe.MekanismRecipeType;

public class AddMekanismRecipe<RECIPE extends MekanismRecipe> extends RecipeMapModification<RECIPE> {

    public AddMekanismRecipe(String name, MekanismRecipeType<RECIPE> recipeType, RECIPE recipe) {
        super(name, true, recipeType);
        recipes.add(recipe);
    }
}