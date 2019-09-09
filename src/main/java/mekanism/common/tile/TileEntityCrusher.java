package mekanism.common.tile;

import javax.annotation.Nonnull;
import mekanism.api.recipes.ItemStackToItemStackRecipe;
import mekanism.common.block.states.BlockStateMachine.MachineType;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.tile.prefab.TileEntityElectricMachine;

public class TileEntityCrusher extends TileEntityElectricMachine {

    public TileEntityCrusher() {
        super("crusher", MachineType.CRUSHER, 200);
    }

    @Nonnull
    @Override
    public Recipe<ItemStackToItemStackRecipe> getRecipes() {
        return Recipe.CRUSHER;
    }
}