package mekanism.common.recipe.lookup;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.IContentsListener;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.lookup.cache.IInputRecipeCache;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public interface IRecipeLookupHandler<RECIPE extends MekanismRecipe> extends IContentsListener {

    /**
     * @return The world for this {@link IRecipeLookupHandler}.
     */
    @Nullable
    default World getHandlerWorld() {
        if (this instanceof TileEntity) {
            return ((TileEntity) this).getLevel();
        } else if (this instanceof Entity) {
            return ((Entity) this).level;
        }
        return null;
    }

    /**
     * @return The recipe type this {@link IRecipeLookupHandler} handles.
     */
    @Nonnull
    MekanismRecipeType<RECIPE, ?> getRecipeType();

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
    @Nonnull
    CachedRecipe<RECIPE> createNewCachedRecipe(@Nonnull RECIPE recipe, int cacheIndex);

    /**
     * Called when the cached recipe changes at a given index before processing the new cached recipe.
     *
     * @param cachedRecipe New cached recipe, or null if there is none due to the caches being invalidated.
     * @param cacheIndex   The "recipe index" for which cache to interact with.
     */
    default void onCachedRecipeChanged(@Nullable CachedRecipe<RECIPE> cachedRecipe, int cacheIndex) {
    }

    /**
     * Helper class that specifies the input cache's type for the recipe type. The reason it isn't defined in the main {@link IRecipeLookupHandler} is it isn't needed and
     * would just make the class definitions a lot messier with very long generics that can be folded away into the helper interfaces we use anyway ofr actual lookup
     * purposes.
     */
    interface IRecipeTypedLookupHandler<RECIPE extends MekanismRecipe, INPUT_CACHE extends IInputRecipeCache> extends IRecipeLookupHandler<RECIPE> {

        @Nonnull
        @Override
        MekanismRecipeType<RECIPE, INPUT_CACHE> getRecipeType();
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