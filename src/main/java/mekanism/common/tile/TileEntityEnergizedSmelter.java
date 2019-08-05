package mekanism.common.tile;

import java.util.Map;
import mekanism.common.MekanismBlock;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.recipe.inputs.ItemStackInput;
import mekanism.common.recipe.machines.SmeltingRecipe;
import mekanism.common.tile.prefab.TileEntityElectricMachine;

public class TileEntityEnergizedSmelter extends TileEntityElectricMachine<SmeltingRecipe> {

    public TileEntityEnergizedSmelter() {
        super(MekanismBlock.ENERGIZED_SMELTER, 200);
    }

    @Override
    public Map<ItemStackInput, SmeltingRecipe> getRecipes() {
        return Recipe.ENERGIZED_SMELTER.get();
    }
}