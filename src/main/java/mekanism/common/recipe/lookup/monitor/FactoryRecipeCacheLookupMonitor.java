package mekanism.common.recipe.lookup.monitor;

import javax.annotation.Nonnull;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.common.recipe.lookup.IRecipeLookupHandler;

public class FactoryRecipeCacheLookupMonitor<RECIPE extends MekanismRecipe> extends RecipeCacheLookupMonitor<RECIPE> {

    private final Runnable setSortingNeeded;

    public FactoryRecipeCacheLookupMonitor(IRecipeLookupHandler<RECIPE> handler, int cacheIndex, Runnable setSortingNeeded) {
        super(handler, cacheIndex);
        this.setSortingNeeded = setSortingNeeded;
    }

    @Override
    public void onContentsChanged() {
        super.onContentsChanged();
        //Mark that sorting is needed
        setSortingNeeded.run();
    }

    public void updateCachedRecipe(@Nonnull RECIPE recipe) {
        cachedRecipe = createNewCachedRecipe(recipe, cacheIndex);
        //Note: While this is probably not strictly needed we clear our cache of knowing we have no recipe
        // so that we can properly re-enter the lookup cycle if needed
        hasNoRecipe = false;
    }

    public void markMayHaveRecipe() {
        //Mark that we may have a recipe again
        hasNoRecipe = false;
    }
}