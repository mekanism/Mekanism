package mekanism.api.recipes.cache;

import mekanism.api.recipes.MekanismRecipe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Interface for helping implement objects that can handle cached recipes.
 */
public interface ICachedRecipeHolder<RECIPE extends MekanismRecipe<?>> {

    /**
     * Gets an updated cache for a given cacheIndex if needed.
     *
     * @param cacheIndex The "recipe index" for which cache to interact with.
     *
     * @return An updated cache for the given cacheIndex, the current cached recipe, or null if there is no current cached recipe or the cache is invalid.
     */
    @Nullable
    default CachedRecipe<RECIPE> getUpdatedCache(int cacheIndex) {
        boolean cacheInvalid = invalidateCache();
        //If we should invalidate the cache, set the current cache to null so that we properly recalculate the values
        // Otherwise lookup what the current cache is set to
        CachedRecipe<RECIPE> currentCache = cacheInvalid ? null : getCachedRecipe(cacheIndex);
        //If there is no cached recipe or the input doesn't match, attempt to get the recipe based on the input
        if (currentCache == null || !currentCache.isInputValid()) {
            if (cacheInvalid || !hasNoRecipe(cacheIndex)) {
                //If our cache is not valid, or we may have a recipe, check if we do
                RECIPE recipe = getRecipe(cacheIndex);
                if (recipe == null) {
                    //If we don't, mark that we have no recipe so that we can cache and short circuit following
                    // recipe lookups until either the cache becomes invalid or we may have a recipe again
                    setHasNoRecipe(cacheIndex);
                } else {
                    //Otherwise, create a new cached recipe
                    CachedRecipe<RECIPE> cached = createNewCachedRecipe(recipe, cacheIndex);
                    if (currentCache == null || cached != null) {
                        //Only override our cached recipe if we were able to find a recipe that matches, or we don't have a cached recipe.
                        // This way if we end up getting back to the same recipe we won't have to recalculate quite as much
                        if (currentCache == null && cached != null) {
                            //If we don't have a current cache try to load any persistent data from the tile
                            loadSavedData(cached, cacheIndex);
                        }
                        return cached;
                    }
                }
            }
        }
        return currentCache;
    }

    /**
     * Used to load any persistent data about the state of the recipe when the holder was saved.
     *
     * @param cached     The recipe to load to.
     * @param cacheIndex The "recipe index" for which cache to interact with.
     */
    default void loadSavedData(@NotNull CachedRecipe<RECIPE> cached, int cacheIndex) {
        cached.loadSavedOperatingTicks(getSavedOperatingTicks(cacheIndex));
    }

    /**
     * Returns how many operating ticks were saved for purposes of persisting through saves how far a cached recipe is through processing.
     *
     * @param cacheIndex The "recipe index" for which cache to interact with.
     *
     * @return Number of operating ticks that had passed before saving.
     */
    default int getSavedOperatingTicks(int cacheIndex) {
        return 0;
    }

    /**
     * Gets the current cached recipe if any for the given cacheIndex.
     *
     * @param cacheIndex The "recipe index" for which cache to interact with.
     *
     * @return Current cached recipe or {@code null} if no recipe is cached or the holder does not handle the given cacheIndex.
     */
    @Nullable
    CachedRecipe<RECIPE> getCachedRecipe(int cacheIndex);

    /**
     * Tries to lookup/get the recipe that a given cacheIndex would represent the slots/tanks/etc. for.
     *
     * @param cacheIndex The "recipe index" for which cache to interact with.
     *
     * @return Recipe that a given cacheIndex corresponds to, or null if there is no matching recipe or if the holder does not handle the given cacheIndex.
     *
     * @implNote This method should ignore what recipe is currently cached as this method is mainly used for finding a backing recipe to create a new cached recipe.
     */
    @Nullable
    RECIPE getRecipe(int cacheIndex);

    /**
     * Creates a new cached recipe representing a given recipe.
     *
     * @param recipe     The backing recipe to create a cached version of.
     * @param cacheIndex The "recipe index" for which cache to interact with.
     *
     * @return A new cached recipe representing the given recipe, or null if the holder does not handle the given cacheIndex.
     */
    @Nullable
    CachedRecipe<RECIPE> createNewCachedRecipe(@NotNull RECIPE recipe, int cacheIndex);

    /**
     * Checks if the cache should be invalidated because it is no longer valid.
     *
     * @return {@code true} if the cache is invalid, {@code false} otherwise.
     */
    default boolean invalidateCache() {
        return false;
    }

    /**
     * Called when a given cacheIndex should be marked as not having a recipe for purposes of speeding up sequential lookup calls when the inputs have not changed.
     *
     * @param cacheIndex The "recipe index" for which cache to interact with.
     */
    default void setHasNoRecipe(int cacheIndex) {
    }

    /**
     * Checks if this cached recipe holder knows that it doesn't have a recipe or if it may have one.
     *
     * @param cacheIndex The "recipe index" for which cache to interact with.
     *
     * @return {@code true} if we know we have no recipe, as nothing has changed since we last checked for a recipe, {@code false} if we may have a recipe or the
     * implementation does not make use of this extra caching.
     */
    default boolean hasNoRecipe(int cacheIndex) {
        return false;
    }
}