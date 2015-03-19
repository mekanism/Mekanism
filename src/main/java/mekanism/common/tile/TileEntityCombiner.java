package mekanism.common.tile;

import java.util.Map;

import mekanism.api.MekanismConfig.usage;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasRegistry;
import mekanism.api.gas.GasStack;
import mekanism.common.block.BlockMachine.MachineType;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.recipe.machines.CombinerRecipe;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class TileEntityCombiner extends TileEntityAdvancedElectricMachine<CombinerRecipe>
{
	public TileEntityCombiner()
	{
		super("combiner", "Combiner", usage.combinerUsage, 1, 200, MachineType.COMBINER.baseEnergy);
	}

	@Override
	public Map getRecipes()
	{
		return Recipe.COMBINER.get();
	}

	@Override
	public GasStack getItemGas(ItemStack itemstack)
	{
		if(itemstack.getItem() instanceof ItemBlock && Block.getBlockFromItem(itemstack.getItem()) == Blocks.cobblestone)
		{
			return new GasStack(GasRegistry.getGas("liquidStone"), 200);
		}

		return null;
	}

	@Override
	public boolean isValidGas(Gas gas)
	{
		return false;
	}
}
