package mekanism.common.tile;

import javax.annotation.Nonnull;
import mekanism.api.recipes.ItemStackToItemStackRecipe;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismMachines;
import mekanism.common.tile.prefab.TileEntityElectricMachine;

public class TileEntityEnergizedSmelter extends TileEntityElectricMachine {

    public TileEntityEnergizedSmelter() {
        super(MekanismMachines.ENERGIZED_SMELTER.getBlockType(), 200);
    }

    @Nonnull
    @Override
    public MekanismRecipeType<ItemStackToItemStackRecipe> getRecipeType() {
        return MekanismRecipeType.SMELTING;
    }
}