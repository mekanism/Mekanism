package mekanism.common;

import java.util.Map;

import mekanism.common.RecipeHandler.Recipe;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

public class TileEntityOsmiumCompressor extends TileEntityAdvancedElectricMachine
{
	public TileEntityOsmiumCompressor()
	{
		super("Compressor.ogg", "Osmium Compressor", "/mods/mekanism/gui/GuiCompressor.png", 10, 1, 200, 2000, 200);
	}
	
	@Override
	public Map getRecipes()
	{
		return Recipe.OSMIUM_COMPRESSOR.get();
	}

	@Override
	public int getFuelTicks(ItemStack itemstack)
	{
		boolean hasOsmium = false;
		
		for(ItemStack ore : OreDictionary.getOres("ingotOsmium"))
		{
			if(ore.isItemEqual(itemstack))
			{
				hasOsmium = true;
				break;
			}
		}
		
		if(hasOsmium) return 200;
		return 0;
	}
}
