package mekanism.common.tile.interfaces;

import javax.annotation.Nonnull;
import mekanism.api.recipes.IMekanismRecipe;
import mekanism.api.recipes.cache.ICachedRecipeHolder;
import mekanism.common.recipe.RecipeHandler.Recipe;

public interface ITileCachedRecipeHolder<RECIPE extends IMekanismRecipe> extends ICachedRecipeHolder<RECIPE> {

    @Nonnull
    Recipe<RECIPE> getRecipes();
}