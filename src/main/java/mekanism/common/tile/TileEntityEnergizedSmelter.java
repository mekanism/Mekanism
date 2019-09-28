package mekanism.common.tile;

import javax.annotation.Nonnull;
import mekanism.api.recipes.ItemStackToItemStackRecipe;
import mekanism.common.MekanismBlock;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.recipe.RecipeHandler.RecipeWrapper;
import mekanism.common.recipe.impl.ItemStackToItemStackIRecipe;
import mekanism.common.tile.prefab.TileEntityElectricMachine;

public class TileEntityEnergizedSmelter extends TileEntityElectricMachine {

    public TileEntityEnergizedSmelter() {
        super(MekanismBlock.ENERGIZED_SMELTER, 200);
    }

    @Nonnull
    @Override
    public Recipe<ItemStackToItemStackRecipe> getRecipes() {
        return Recipe.ENERGIZED_SMELTER;
    }

    @Nonnull
    @Override
    public RecipeWrapper<ItemStackToItemStackIRecipe> getRecipeWrapper() {
        return RecipeWrapper.ENERGIZED_SMELTER;
    }
}