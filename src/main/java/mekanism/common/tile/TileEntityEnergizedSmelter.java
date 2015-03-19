package mekanism.common.tile;

import java.util.Map;

import mekanism.api.MekanismConfig.usage;
import mekanism.common.block.BlockMachine.MachineType;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.recipe.inputs.ItemStackInput;
import mekanism.common.recipe.machines.SmeltingRecipe;

public class TileEntityEnergizedSmelter extends TileEntityElectricMachine<SmeltingRecipe>
{
	public TileEntityEnergizedSmelter()
	{
		super("smelter", "EnergizedSmelter", usage.energizedSmelterUsage, 200, MachineType.ENERGIZED_SMELTER.baseEnergy);
	}

	@Override
	public Map<ItemStackInput, SmeltingRecipe> getRecipes()
	{
		return Recipe.ENERGIZED_SMELTER.get();
	}
}
