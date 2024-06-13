package mekanism.common.recipe.lookup;

import mekanism.api.IContentsListener;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.client.recipe_viewer.type.IRecipeViewerRecipeType;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import mekanism.common.recipe.lookup.cache.IInputRecipeCache;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IRecipeLookupHandler<RECIPE extends MekanismRecipe<?>> extends IContentsListener {

    /**
     * @return The world for this {@link IRecipeLookupHandler}.
     */
    @Nullable
    Level getLevel();

    /**
     * @return The recipe type this {@link IRecipeLookupHandler} handles.
     */
    @NotNull
    IMekanismRecipeTypeProvider<?, RECIPE, ?> getRecipeType();

    @Nullable
    default IRecipeViewerRecipeType<RECIPE> recipeViewerType() {
        return null;
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
     * Tries to lookup/get the recipe that a given cacheIndex would represent the slots/tanks/etc. for.
     *
     * @param cacheIndex The "recipe index" for which cache to interact with.
     *
     * @return Recipe that a given cacheIndex corresponds to, or null if there is no matching recipe.
     */
    @Nullable
    RECIPE getRecipe(int cacheIndex);

    /**
     * Creates a new cached recipe representing a given recipe.
     *
     * @param recipe     The backing recipe to create a cached version of.
     * @param cacheIndex The "recipe index" for which cache to interact with.
     *
     * @return A new cached recipe representing the given recipe.
     */
    @NotNull
    CachedRecipe<RECIPE> createNewCachedRecipe(@NotNull RECIPE recipe, int cacheIndex);

    /**
     * Called when the cached recipe changes at a given index before processing the new cached recipe.
     *
     * @param cachedRecipe New cached recipe, or null if there is none due to the caches being invalidated.
     * @param cacheIndex   The "recipe index" for which cache to interact with.
     */
    default void onCachedRecipeChanged(@Nullable CachedRecipe<RECIPE> cachedRecipe, int cacheIndex) {
        clearRecipeErrors(cacheIndex);
    }

    /**
     * Called by {@link #onCachedRecipeChanged(CachedRecipe, int)} when the list of cached errors should be reset due to the recipe not being valid any more.
     *
     * @param cacheIndex The "recipe index" for which cache to interact with.
     */
    default void clearRecipeErrors(int cacheIndex) {
    }

    /**
     * Helper class that specifies the input cache's type for the recipe type. The reason it isn't defined in the main {@link IRecipeLookupHandler} is it isn't needed and
     * would just make the class definitions a lot messier with very long generics that can be folded away into the helper interfaces we use anyway ofr actual lookup
     * purposes.
     */
    interface IRecipeTypedLookupHandler<RECIPE extends MekanismRecipe<?>, INPUT_CACHE extends IInputRecipeCache> extends IRecipeLookupHandler<RECIPE> {

        @NotNull
        @Override
        IMekanismRecipeTypeProvider<?, RECIPE, INPUT_CACHE> getRecipeType();
    }

    interface ConstantUsageRecipeLookupHandler {

        /**
         * Returns how much of the constant secondary input had been used for purposes of persisting through saves how far a cached recipe is through processing.
         *
         * @param cacheIndex The "recipe index" for which cache to interact with.
         *
         * @return Constant amount of secondary input that had been used before saving.
         */
        default long getSavedUsedSoFar(int cacheIndex) {
            return 0;
        }
    }
}