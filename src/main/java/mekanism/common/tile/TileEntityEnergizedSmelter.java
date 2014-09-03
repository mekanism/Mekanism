package mekanism.common.tile;

import java.util.HashMap;
import java.util.Map;

import mekanism.api.MekanismConfig.usage;
import mekanism.common.block.BlockMachine.MachineType;

import net.minecraft.item.ItemStack;

public class TileEntityEnergizedSmelter extends TileEntityElectricMachine
{
	public static Map<ItemStack, ItemStack> furnaceRecipes = new HashMap<ItemStack, ItemStack>();

	public TileEntityEnergizedSmelter()
	{
		super("smelter", "EnergizedSmelter", usage.energizedSmelterUsage, 200, MachineType.ENERGIZED_SMELTER.baseEnergy);
	}

	@Override
	public Map getRecipes()
	{
		return furnaceRecipes;
	}
}
