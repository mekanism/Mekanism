package mekanism.common.tile.prefab;

import javax.annotation.Nullable;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.common.tile.interfaces.ITileCachedRecipeHolder;

public abstract class TileEntityRecipeMachine<RECIPE extends MekanismRecipe> extends TileEntityConfigurableMachine implements ITileCachedRecipeHolder<RECIPE> {

    protected CachedRecipe<RECIPE> cachedRecipe = null;

    protected TileEntityRecipeMachine(IBlockProvider blockProvider) {
        super(blockProvider);
    }

    @Nullable
    @Override
    public CachedRecipe<RECIPE> getCachedRecipe(int cacheIndex) {
        return cachedRecipe;
    }

    @Override
    public boolean renderUpdate() {
        return true;
    }

    @Override
    public boolean lightUpdate() {
        return true;
    }
}