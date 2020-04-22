package mekanism.common.tile.prefab;

import mekanism.api.providers.IBlockProvider;
import mekanism.api.recipes.MekanismRecipe;

public abstract class TileEntityGasToGasMachine<RECIPE extends MekanismRecipe> extends TileEntityRecipeMachine<RECIPE> {

    protected TileEntityGasToGasMachine(IBlockProvider blockProvider) {
        super(blockProvider);
    }
}