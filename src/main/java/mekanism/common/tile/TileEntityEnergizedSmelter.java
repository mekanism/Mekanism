package mekanism.common.tile;

import java.util.Map;

import mekanism.common.block.states.BlockStateMachine;
import mekanism.common.config.MekanismConfig.usage;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.recipe.inputs.ItemStackInput;
import mekanism.common.recipe.machines.SmeltingRecipe;

public class TileEntityEnergizedSmelter extends TileEntityElectricMachine<SmeltingRecipe>
{
	public TileEntityEnergizedSmelter()
	{
		super("smelter", "EnergizedSmelter", usage.energizedSmelterUsage, 200, BlockStateMachine.MachineType.ENERGIZED_SMELTER.baseEnergy);
	}

	@Override
	public Map<ItemStackInput, SmeltingRecipe> getRecipes()
	{
		return Recipe.ENERGIZED_SMELTER.get();
	}
}
