package mekanism.common.recipe.lookup.monitor;

import mekanism.api.recipes.MekanismRecipe;
import mekanism.common.recipe.lookup.IRecipeLookupHandler;
import net.minecraft.world.item.crafting.RecipeHolder;

public class FactoryRecipeCacheLookupMonitor<RECIPE extends MekanismRecipe<?, ?, ?>> extends ProgressRecipeCacheLookupMonitor<RECIPE> {

    private final Runnable setSortingNeeded;

    public FactoryRecipeCacheLookupMonitor(IRecipeLookupHandler<RECIPE> handler, int cacheIndex, Runnable setSortingNeeded) {
        super(handler, cacheIndex);
        this.setSortingNeeded = setSortingNeeded;
    }

    @Override
    public void onChange() {
        super.onChange();
        //Mark that sorting is needed
        setSortingNeeded.run();
    }

    public void updateCachedRecipe(RecipeHolder<RECIPE> recipeHolder) {
        cachedRecipe = createNewCachedRecipe(recipeHolder, cacheIndex);
        //Note: While this is probably not strictly needed we clear our cache of knowing we have no recipe
        // so that we can properly re-enter the lookup cycle if needed
        hasNoRecipe = false;
    }
}