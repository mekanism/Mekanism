package mekanism.common.tile;

import javax.annotation.Nonnull;
import mekanism.api.recipes.ItemStackToItemStackRecipe;
import mekanism.common.MekanismBlock;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.recipe.RecipeHandler.RecipeWrapper;
import mekanism.common.tile.prefab.TileEntityElectricMachine;

public class TileEntityCrusher extends TileEntityElectricMachine {

    public TileEntityCrusher() {
        super(MekanismBlock.CRUSHER, 200);
    }

    @Nonnull
    @Override
    public Recipe<ItemStackToItemStackRecipe> getRecipes() {
        return Recipe.CRUSHER;
    }

    @Nonnull
    @Override
    public RecipeWrapper<ItemStackToItemStackRecipe> getRecipeWrapper() {
        return RecipeWrapper.CRUSHING;
    }
}