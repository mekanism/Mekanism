package mekanism.common.tile.interfaces;

import javax.annotation.Nonnull;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.cache.ICachedRecipeHolder;
import mekanism.common.recipe.RecipeHandler.Recipe;

public interface ITileCachedRecipeHolder<RECIPE extends MekanismRecipe> extends ICachedRecipeHolder<RECIPE> {

    @Nonnull
    Recipe<RECIPE> getRecipes();
}