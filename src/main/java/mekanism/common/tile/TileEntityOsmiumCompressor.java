package mekanism.common.tile;

import javax.annotation.Nonnull;
import mekanism.api.recipes.ItemStackGasToItemStackRecipe;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.prefab.TileEntityAdvancedElectricMachine;

public class TileEntityOsmiumCompressor extends TileEntityAdvancedElectricMachine {

    public TileEntityOsmiumCompressor() {
        super(MekanismBlocks.OSMIUM_COMPRESSOR, BASE_TICKS_REQUIRED, BASE_GAS_PER_TICK);
    }

    @Nonnull
    @Override
    public MekanismRecipeType<ItemStackGasToItemStackRecipe> getRecipeType() {
        return MekanismRecipeType.COMPRESSING;
    }
}