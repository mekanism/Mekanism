package mekanism.common;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.item.ItemStack;

public class TileEntityPlatinumCompressor extends TileEntityAdvancedElectricMachine
{
	public static Map<ItemStack, ItemStack> recipes = new HashMap<ItemStack, ItemStack>();
	
	public TileEntityPlatinumCompressor()
	{
		super("Compressor.ogg", "Platinum Compressor", "/resources/mekanism/gui/GuiCompressor.png", 5, 1, 200, 1000, 200);
	}

	@Override
	public Map getRecipes()
	{
		return recipes;
	}

	@Override
	public int getFuelTicks(ItemStack itemstack)
	{
		if (itemstack.itemID == new ItemStack(Mekanism.Ingot, 1, 1).itemID) return 200;
		return 0;
	}
}
