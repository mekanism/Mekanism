package mekanism.common.integration.crafttweaker.util;

import mekanism.api.recipes.IMekanismRecipe;
import mekanism.common.recipe.RecipeHandler.Recipe;

public class RemoveAllMekanismRecipe<RECIPE extends IMekanismRecipe> extends RecipeMapModification<RECIPE> {

    public RemoveAllMekanismRecipe(String name, Recipe<RECIPE> recipeType) {
        super(name, false, recipeType);
    }

    @Override
    public void apply() {
        //Don't move this into the constructor so that if an addon registers recipes late, we can still remove them
        recipes.addAll(recipeType.get());
        super.apply();
    }

    @Override
    public String describe() {
        return "Removed all recipes for " + name;
    }
}