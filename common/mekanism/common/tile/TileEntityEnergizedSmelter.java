package mekanism.common.tile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mekanism.common.Mekanism;
import mekanism.common.block.BlockMachine.MachineType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.util.ResourceLocation;

public class TileEntityEnergizedSmelter extends TileEntityElectricMachine
{
	public static Map<ItemStack, ItemStack> furnaceRecipes = new HashMap<ItemStack, ItemStack>();

	public TileEntityEnergizedSmelter()
	{
		super("Smelter.ogg", "EnergizedSmelter", new ResourceLocation("mekanism", "gui/GuiEnergizedSmelter.png"), Mekanism.energizedSmelterUsage, 200, MachineType.ENERGIZED_SMELTER.baseEnergy);
	}

	@Override
	public Map getRecipes()
	{
		return furnaceRecipes;
	}
}
