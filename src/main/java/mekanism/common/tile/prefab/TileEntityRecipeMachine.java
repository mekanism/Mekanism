package mekanism.common.tile.prefab;

import mekanism.api.providers.IBlockProvider;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.common.recipe.lookup.IRecipeLookupHandler;
import mekanism.common.recipe.lookup.monitor.RecipeCacheLookupMonitor;

public abstract class TileEntityRecipeMachine<RECIPE extends MekanismRecipe> extends TileEntityConfigurableMachine implements IRecipeLookupHandler<RECIPE> {

    protected RecipeCacheLookupMonitor<RECIPE> recipeCacheLookupMonitor;

    protected TileEntityRecipeMachine(IBlockProvider blockProvider) {
        super(blockProvider);
    }

    @Override
    protected void presetVariables() {
        super.presetVariables();
        recipeCacheLookupMonitor = createNewCacheMonitor();
    }

    protected RecipeCacheLookupMonitor<RECIPE> createNewCacheMonitor() {
        return new RecipeCacheLookupMonitor<>(this);
    }
}