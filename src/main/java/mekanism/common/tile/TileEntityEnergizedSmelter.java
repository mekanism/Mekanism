package mekanism.common.tile;

import java.util.Map;

import mekanism.api.MekanismConfig.usage;
import mekanism.common.block.states.BlockStateMachine;
import mekanism.common.block.states.BlockStateMachine.MachineBlockType;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.recipe.inputs.ItemStackInput;
import mekanism.common.recipe.machines.SmeltingRecipe;

public class TileEntityEnergizedSmelter extends TileEntityElectricMachine<SmeltingRecipe>
{
	public TileEntityEnergizedSmelter()
	{
		super("smelter", "EnergizedSmelter", 200, MachineBlockType.ENERGIZED_SMELTER);
	}

	@Override
	public Map<ItemStackInput, SmeltingRecipe> getRecipes()
	{
		return Recipe.ENERGIZED_SMELTER.get();
	}
}
