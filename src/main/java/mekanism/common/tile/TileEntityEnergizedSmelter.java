package mekanism.common.tile;

import java.util.List;
import mekanism.api.recipes.ItemStackToItemStackRecipe;
import mekanism.common.block.states.BlockStateMachine.MachineType;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.tile.prefab.TileEntityElectricMachine;

public class TileEntityEnergizedSmelter extends TileEntityElectricMachine<ItemStackToItemStackRecipe> {

    public TileEntityEnergizedSmelter() {
        super("smelter", MachineType.ENERGIZED_SMELTER, 200);
    }

    @Override
    public List<ItemStackToItemStackRecipe> getRecipes() {
        return Recipe.ENERGIZED_SMELTER.get();
    }
}