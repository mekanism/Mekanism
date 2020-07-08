package mekanism.api.recipes.cache;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.recipes.MekanismRecipe;

//TODO: JavaDocs
// Note: The cacheIndex is for purposes of what cache it will be stored in
public interface ICachedRecipeHolder<RECIPE extends MekanismRecipe> {

    @Nullable
    default CachedRecipe<RECIPE> getUpdatedCache(int cacheIndex) {
        CachedRecipe<RECIPE> currentCache = getCachedRecipe(cacheIndex);
        if (invalidateCache()) {
            //If we should invalidate the cache, set the current cache to null so that we properly recalculate the values
            currentCache = null;
        }
        //TODO: We probably want something so that we don't have to recheck if the input has not changed since the last check
        // Maybe make it be !isInputValid AND inputHasChanged to check if the input is the same as the last time isInputValid was called
        // Note: This still doesn't have a benefit for if currentCache is null, but it will still help overall, or maybe make a method in this interface
        // that has to be implemented, giving thinking about it there is no reason input/output helpers cannot be final given they are just suppliers
        // and are always the same for a given machine
        //If there is no cached recipe or the input doesn't match, attempt to get the recipe based on the input
        if (currentCache == null || !currentCache.isInputValid()) {
            RECIPE recipe = getRecipe(cacheIndex);
            if (recipe != null) {
                CachedRecipe<RECIPE> cached = createNewCachedRecipe(recipe, cacheIndex);
                if (currentCache == null || cached != null) {
                    //Only override our cached recipe if we were able to find a recipe that matches, or we don't have a cached recipe.
                    // This way if we end up getting back to the same recipe we won't have to recalculate quite as much
                    if (currentCache == null && cached != null) {
                        //If we don't have a current cache try loading our operating ticks from the tile
                        cached.loadSavedOperatingTicks(getSavedOperatingTicks(cacheIndex));
                    }
                    return cached;
                }
            }
        }
        return currentCache;
    }

    default int getSavedOperatingTicks(int cacheIndex) {
        return 0;
    }

    @Nullable
    CachedRecipe<RECIPE> getCachedRecipe(int cacheIndex);

    //TODO: Decide if we should have these methods fail if an invalid cache index is given to machines that only cache one recipe
    //TODO: This method gets recipe from inputs in machine ignoring what is currently cached mainly for purposes of creating the new cached recipe
    @Nullable
    RECIPE getRecipe(int cacheIndex);

    //TODO: Why is this nullable??
    @Nullable
    CachedRecipe<RECIPE> createNewCachedRecipe(@Nonnull RECIPE recipe, int cacheIndex);

    default boolean invalidateCache() {
        return false;
    }
}