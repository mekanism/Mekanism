package mekanism.api.recipes.cache;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.recipes.IMekanismRecipe;
import mekanism.common.recipe.RecipeHandler.Recipe;

//TODO: JavaDocs
public interface ICachedRecipeHolder<RECIPE extends IMekanismRecipe> {

    @Nonnull
    Recipe<RECIPE> getRecipes();

    @Nullable
    default CachedRecipe<RECIPE> getUpdatedCache(@Nullable CachedRecipe<RECIPE> currentCache) {
        //If there is no cached recipe or the input doesn't match, attempt to get the recipe based on the input
        if (currentCache == null || !currentCache.hasResourcesForTick()) {
            //TODO: Should this use a separate method than hasResourcesForTick?
            RECIPE recipe = getRecipe();
            if (recipe != null) {
                CachedRecipe<RECIPE> cached = createNewCachedRecipe(recipe);
                if (currentCache == null || cached != null) {
                    //Only override our cached recipe if we were able to find a recipe that matches, or we don't have a cached recipe.
                    // This way if we end up getting back to the same recipe we won't have to recalculate quite as much
                    return cached;
                }
            }
        }
        return currentCache;
    }

    //TODO: This method gets recipe from inputs in machine ignoring what is currently cached mainly for purposes of creating the new cached recipe
    @Nullable
    RECIPE getRecipe();

    CachedRecipe<RECIPE> createNewCachedRecipe(@Nonnull RECIPE recipe);
}