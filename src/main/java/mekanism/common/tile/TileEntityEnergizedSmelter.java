package mekanism.common.tile;

import java.util.Map;
import mekanism.common.block.states.BlockStateMachine.MachineType;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.recipe.inputs.ItemStackInput;
import mekanism.common.recipe.machines.SmeltingRecipe;
import mekanism.common.tile.prefab.TileEntityElectricMachine;

public class TileEntityEnergizedSmelter extends TileEntityElectricMachine<SmeltingRecipe> {

    private static Map<ItemStackInput, SmeltingRecipe> cachedRecipes;

    public TileEntityEnergizedSmelter() {
        super("smelter", "EnergizedSmelter",
                MachineType.ENERGIZED_SMELTER.getStorage(),
                MachineType.ENERGIZED_SMELTER.getUsage(), 200);
    }

    @Override
    public Map<ItemStackInput, SmeltingRecipe> getRecipes() {
        return Recipe.ENERGIZED_SMELTER.get();
    }
}
