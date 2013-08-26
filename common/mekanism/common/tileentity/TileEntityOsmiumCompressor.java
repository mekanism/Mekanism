package mekanism.common.tileentity;

import java.util.Map;

import mekanism.common.Mekanism;
import mekanism.common.RecipeHandler.Recipe;
import mekanism.common.block.BlockMachine.MachineType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.oredict.OreDictionary;

public class TileEntityOsmiumCompressor extends TileEntityAdvancedElectricMachine
{
	public TileEntityOsmiumCompressor()
	{
		super("Compressor.ogg", "Osmium Compressor", new ResourceLocation("mekanism", "gui/GuiCompressor.png"), Mekanism.osmiumCompressorUsage, 1, 200, MachineType.OSMIUM_COMPRESSOR.baseEnergy, 200);
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
