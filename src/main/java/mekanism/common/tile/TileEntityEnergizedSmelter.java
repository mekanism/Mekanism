package mekanism.common.tile;

import java.util.HashMap;
import java.util.Map;

import mekanism.common.block.states.BlockStateMachine;
import mekanism.common.config.MekanismConfig.usage;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.recipe.inputs.ItemStackInput;
import mekanism.common.recipe.machines.SmeltingRecipe;
import mekanism.common.recipe.outputs.ItemStackOutput;
import mekanism.common.tile.prefab.TileEntityElectricMachine;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;

public class TileEntityEnergizedSmelter extends TileEntityElectricMachine<SmeltingRecipe>
{
	private static Map<ItemStackInput, SmeltingRecipe> cachedRecipes;

	public TileEntityEnergizedSmelter()
	{
		super("smelter", "EnergizedSmelter", BlockStateMachine.MachineType.ENERGIZED_SMELTER.baseEnergy, usage.energizedSmelterUsage, 200);
	}

	@Override
	public synchronized Map<ItemStackInput, SmeltingRecipe> getRecipes()
	{
		if(cachedRecipes == null)
		{
			cachedRecipes = new HashMap<>();
			cachedRecipes.putAll(Recipe.ENERGIZED_SMELTER.get());
			for(Map.Entry<ItemStack, ItemStack> entry : FurnaceRecipes.instance().getSmeltingList().entrySet())
			{
				SmeltingRecipe recipe = new SmeltingRecipe(new ItemStackInput(entry.getKey()), new ItemStackOutput(entry.getValue()));
				cachedRecipes.put(recipe.getInput(), recipe);
			}
		}
		return cachedRecipes;
	}
}
