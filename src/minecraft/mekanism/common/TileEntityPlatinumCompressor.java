package mekanism.common;

import java.util.HashMap;
import java.util.Map;

import mekanism.common.RecipeHandler.Recipe;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

public class TileEntityPlatinumCompressor extends TileEntityAdvancedElectricMachine
{
	public TileEntityPlatinumCompressor()
	{
		super("Compressor.ogg", "Platinum Compressor", "/resources/mekanism/gui/GuiCompressor.png", 10, 1, 200, 2000, 200);
	}
	
	@Override
	public Map getRecipes()
	{
		return Recipe.PLATINUM_COMPRESSOR.get();
	}

	@Override
	public int getFuelTicks(ItemStack itemstack)
	{
		boolean hasPlatinum = false;
		
		for(ItemStack ore : OreDictionary.getOres("ingotPlatinum"))
		{
			if(ore.isItemEqual(itemstack))
			{
				hasPlatinum = true;
				break;
			}
		}
		
		if(hasPlatinum) return 200;
		return 0;
	}
}
