package mekanism.api.recipes.cache;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.recipes.IMekanismRecipe;
import mekanism.common.recipe.RecipeHandler.Recipe;

//TODO: JavaDocs
// Note: The cacheIndex is for purposes of what cache it will be stored in
public interface ICachedRecipeHolder<RECIPE extends IMekanismRecipe> {

    //TODO: This is actually in error AS Recipe does not exist in the scope of the api package
    // Technically it is not *needed* by anything except is useful for the implementations of some of the things
    // the interface declares
    @Nonnull
    Recipe<RECIPE> getRecipes();

    //TODO: Should we have a getCurrentCache(int cacheIndex)? Probably would just make it messier to be honest

    @Nullable
    default CachedRecipe<RECIPE> getUpdatedCache(@Nullable CachedRecipe<RECIPE> currentCache, int cacheIndex) {
        //If there is no cached recipe or the input doesn't match, attempt to get the recipe based on the input
        if (currentCache == null || !currentCache.isInputValid()) {
            //TODO: Should this use a separate method than hasResourcesForTick?
            RECIPE recipe = getRecipe(cacheIndex);
            if (recipe != null) {
                CachedRecipe<RECIPE> cached = createNewCachedRecipe(recipe, cacheIndex);
                if (currentCache == null || cached != null) {
                    //Only override our cached recipe if we were able to find a recipe that matches, or we don't have a cached recipe.
                    // This way if we end up getting back to the same recipe we won't have to recalculate quite as much
                    return cached;
                }
            }
        }
        return currentCache;
    }

    //TODO: Decide if we should have these methods fail if an invalid cache index is given to machines that only cache one recipe
    //TODO: This method gets recipe from inputs in machine ignoring what is currently cached mainly for purposes of creating the new cached recipe
    @Nullable
    RECIPE getRecipe(int cacheIndex);

    //TODO: Why is this nullable??
    @Nullable
    CachedRecipe<RECIPE> createNewCachedRecipe(@Nonnull RECIPE recipe, int cacheIndex);
}