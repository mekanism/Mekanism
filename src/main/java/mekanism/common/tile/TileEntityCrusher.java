package mekanism.common.tile;

import mekanism.api.recipes.ItemStackToItemStackRecipe;
import mekanism.common.block.states.BlockStateMachine.MachineType;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.tile.prefab.TileEntityElectricMachine;

public class TileEntityCrusher extends TileEntityElectricMachine {

    public TileEntityCrusher() {
        super("crusher", MachineType.CRUSHER, 200);
    }

    @Override
    public Recipe<ItemStackToItemStackRecipe> getRecipes() {
        return Recipe.CRUSHER;
    }
}