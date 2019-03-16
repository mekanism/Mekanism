package mekanism.common.tile;

import java.util.Map;

import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.common.MekanismFluids;
import mekanism.common.block.states.BlockStateMachine;
import mekanism.common.config.MekanismConfig;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.recipe.inputs.AdvancedMachineInput;
import mekanism.common.recipe.machines.OsmiumCompressorRecipe;
import mekanism.common.tile.prefab.TileEntityAdvancedElectricMachine;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

public class TileEntityOsmiumCompressor extends TileEntityAdvancedElectricMachine<OsmiumCompressorRecipe>
{
	public TileEntityOsmiumCompressor()
	{
		super("compressor", "OsmiumCompressor", BlockStateMachine.MachineType.OSMIUM_COMPRESSOR.baseEnergy, MekanismConfig.current().usage.osmiumCompressorUsage.val(), 200, 1);
	}

	@Override
	public Map<AdvancedMachineInput, OsmiumCompressorRecipe> getRecipes()
	{
		return Recipe.OSMIUM_COMPRESSOR.get();
	}

	@Override
	public GasStack getItemGas(ItemStack itemstack)
	{
		int amount = 0;

		for(ItemStack ore : OreDictionary.getOres("ingotOsmium"))
		{
			if(ore.isItemEqual(itemstack))
			{
				return new GasStack(MekanismFluids.LiquidOsmium, 200);
			}
		}

		for(ItemStack ore : OreDictionary.getOres("blockOsmium"))
		{
			if(ore.isItemEqual(itemstack))
			{
				return new GasStack(MekanismFluids.LiquidOsmium, 1800);
			}
		}

		return null;
	}

	@Override
	public boolean isValidGas(Gas gas)
	{
		return false;
	}
}
