package mekanism.common.integration.crafttweaker.util;

import mekanism.api.recipes.MekanismRecipe;
import mekanism.common.recipe.RecipeHandler.Recipe;

public class AddMekanismRecipe<RECIPE extends MekanismRecipe> extends RecipeMapModification<RECIPE> {

    public AddMekanismRecipe(String name, Recipe<RECIPE> recipeType, RECIPE recipe) {
        super(name, true, recipeType);
        recipes.add(recipe);
    }
}