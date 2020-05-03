package mekanism.common.tile.machine;

import javax.annotation.Nonnull;
import mekanism.api.recipes.ItemStackToItemStackRecipe;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.prefab.TileEntityElectricMachine;

public class TileEntityEnergizedSmelter extends TileEntityElectricMachine {

    public TileEntityEnergizedSmelter() {
        super(MekanismBlocks.ENERGIZED_SMELTER, 200);
    }

    @Nonnull
    @Override
    public MekanismRecipeType<ItemStackToItemStackRecipe> getRecipeType() {
        return MekanismRecipeType.SMELTING;
    }
}