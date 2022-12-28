package mekanism.common.tile;

import java.util.Map;

import mekanism.api.MekanismConfig;
import mekanism.api.MekanismConfig.usage;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasRegistry;
import mekanism.api.gas.GasStack;
import mekanism.common.block.BlockMachine.MachineType;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.recipe.machines.OsmiumCompressorRecipe;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

public class TileEntityOsmiumCompressor extends TileEntityAdvancedElectricMachine<OsmiumCompressorRecipe>
{
	public TileEntityOsmiumCompressor()
	{
		super("compressor", "OsmiumCompressor", usage.osmiumCompressorUsage, 1, 200, MachineType.OSMIUM_COMPRESSOR.baseEnergy);
		MAX_GAS = 200; //FIXME with a better fix
	}

	@Override
	public Map getRecipes()
	{
		return Recipe.OSMIUM_COMPRESSOR.get();
	}

	@Override
	public GasStack getItemGas(ItemStack itemstack)
	{
		int amount = 0;
		boolean replaceWithPlat = MekanismConfig.general.OreDictPlatinum && !MekanismConfig.general.OreDictOsmium;
		
		if (replaceWithPlat) {
			for (ItemStack ore : OreDictionary.getOres("ingotPlatinum")) {
				if (ore.isItemEqual(itemstack)) {
					return new GasStack(GasRegistry.getGas("liquidPlatinum"), 200);
				}
			}

			for (ItemStack ore : OreDictionary.getOres("blockPlatinum")) {
				if (ore.isItemEqual(itemstack)) {
					return new GasStack(GasRegistry.getGas("liquidPlatinum"), 1800);
				}
			}
		} else {
		for (ItemStack ore : OreDictionary.getOres("ingotOsmium")) {
			if (ore.isItemEqual(itemstack)) {
				return new GasStack(GasRegistry.getGas("liquidOsmium"), 200);
			}
		}

		for (ItemStack ore : OreDictionary.getOres("blockOsmium")) {
			if (ore.isItemEqual(itemstack)) {
				return new GasStack(GasRegistry.getGas("liquidOsmium"), 1800);
			}
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
