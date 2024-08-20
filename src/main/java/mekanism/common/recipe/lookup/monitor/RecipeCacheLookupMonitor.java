package mekanism.common.recipe.lookup.monitor;

import mekanism.api.IContentsListener;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.cache.ICachedRecipeHolder;
import mekanism.api.recipes.cache.ItemStackConstantChemicalToObjectCachedRecipe;
import mekanism.common.CommonWorldTickHandler;
import mekanism.common.recipe.lookup.IRecipeLookupHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RecipeCacheLookupMonitor<RECIPE extends MekanismRecipe<?>> implements ICachedRecipeHolder<RECIPE>, IContentsListener {

    private final IRecipeLookupHandler<RECIPE> handler;
    protected final int cacheIndex;
    protected CachedRecipe<RECIPE> cachedRecipe;
    protected boolean hasNoRecipe;
    protected boolean shouldUnpause;

    public RecipeCacheLookupMonitor(IRecipeLookupHandler<RECIPE> handler) {
        this(handler, 0);
    }

    public RecipeCacheLookupMonitor(IRecipeLookupHandler<RECIPE> handler, int cacheIndex) {
        this.handler = handler;
        this.cacheIndex = cacheIndex;
    }

    protected boolean cachedIndexMatches(int cacheIndex) {
        return this.cacheIndex == cacheIndex;
    }

    @Override
    public final void onContentsChanged() {
        handler.onContentsChanged();
        onChange();
    }

    public void onChange() {
        //Mark that we may have a recipe again
        hasNoRecipe = false;
        unpause();
    }

    public void unpause() {
        shouldUnpause = true;
    }

    /**
     * Helper that wraps {@link #updateAndProcess()} inside of a brief check to calculate how much energy actually got used.
     */
    public long updateAndProcess(IEnergyContainer energyContainer) {
        //Copy this so that if it changes we still have the original amount. Don't bother making it a constant though as this way
        // we can then use minusEqual instead of subtract to remove an extra copy call
        long prev = energyContainer.getEnergy();
        if (updateAndProcess()) {
            //Update amount of energy that actually got used, as if we are "near" full we may not have performed our max number of operations
            return Math.max(0, prev - energyContainer.getEnergy());
        }
        //If we don't have a cached recipe so didn't process anything at all just return zero
        return 0L;
    }

    public boolean updateAndProcess() {
        CachedRecipe<RECIPE> oldCache = cachedRecipe;
        cachedRecipe = getUpdatedCache(cacheIndex);
        if (cachedRecipe != oldCache) {
            handler.onCachedRecipeChanged(cachedRecipe, cacheIndex);
        }
        if (cachedRecipe != null) {
            if (shouldUnpause) {
                shouldUnpause = false;
                cachedRecipe.unpauseErrors();
            }
            cachedRecipe.process();
            return true;
        }
        return false;
    }

    @Override
    public void loadSavedData(@NotNull CachedRecipe<RECIPE> cached, int cacheIndex) {
        if (cachedIndexMatches(cacheIndex)) {
            ICachedRecipeHolder.super.loadSavedData(cached, cacheIndex);
            if (cached instanceof ItemStackConstantChemicalToObjectCachedRecipe<?, ?> c &&
                handler instanceof IRecipeLookupHandler.ConstantUsageRecipeLookupHandler lookupHandler) {
                c.loadSavedUsageSoFar(lookupHandler.getSavedUsedSoFar(cacheIndex));
            }
        }
    }

    @Override
    public int getSavedOperatingTicks(int cacheIndex) {
        return cachedIndexMatches(cacheIndex) ? handler.getSavedOperatingTicks(cacheIndex) : ICachedRecipeHolder.super.getSavedOperatingTicks(cacheIndex);
    }

    @Nullable
    @Override
    public CachedRecipe<RECIPE> getCachedRecipe(int cacheIndex) {
        return cachedIndexMatches(cacheIndex) ? cachedRecipe : null;
    }

    @Nullable
    @Override
    public RECIPE getRecipe(int cacheIndex) {
        return cachedIndexMatches(cacheIndex) ? handler.getRecipe(cacheIndex) : null;
    }

    @Nullable
    @Override
    public CachedRecipe<RECIPE> createNewCachedRecipe(@NotNull RECIPE recipe, int cacheIndex) {
        return cachedIndexMatches(cacheIndex) ? handler.createNewCachedRecipe(recipe, cacheIndex) : null;
    }

    @Override
    public boolean invalidateCache() {
        return CommonWorldTickHandler.flushTagAndRecipeCaches;
    }

    @Override
    public void setHasNoRecipe(int cacheIndex) {
        if (cachedIndexMatches(cacheIndex)) {
            hasNoRecipe = true;
        }
    }

    @Override
    public boolean hasNoRecipe(int cacheIndex) {
        return cachedIndexMatches(cacheIndex) ? hasNoRecipe : ICachedRecipeHolder.super.hasNoRecipe(cacheIndex);
    }
}