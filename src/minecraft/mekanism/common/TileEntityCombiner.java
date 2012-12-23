package mekanism.common;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class TileEntityCombiner extends TileEntityAdvancedElectricMachine
{
	public static Map<ItemStack, ItemStack> recipes = new HashMap<ItemStack, ItemStack>();
	
	public TileEntityCombiner()
	{
		super("Combiner.ogg", "Combiner", "/resources/mekanism/gui/GuiCombiner.png", 5, 1, 200, 1000, 200);
	}
	
	@Override
	public Map getRecipes()
	{
		return recipes;
	}
	
	@Override
	public int getFuelTicks(ItemStack itemstack)
	{
		if(itemstack.getItem() instanceof ItemBlock && itemstack.itemID == Block.cobblestone.blockID)
		{
			return 200;
		}
		return 0;
	}
}
