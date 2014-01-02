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
		super("Compressor.ogg", "OsmiumCompressor", new ResourceLocation("mekanism", "gui/GuiCompressor.png"), Mekanism.osmiumCompressorUsage, 1, 200, MachineType.OSMIUM_COMPRESSOR.baseEnergy, 200);
	}
	
	@Override
	public Map getRecipes()
	{
		return Recipe.OSMIUM_COMPRESSOR.get();
	}

	@Override
	public int getFuelTicks(ItemStack itemstack)
	{
		int amount = 0;
		
		for(ItemStack ore : OreDictionary.getOres("ingotOsmium"))
		{
			if(ore.isItemEqual(itemstack))
			{
				amount = 200;
				break;
			}
		}
		
		for(ItemStack ore : OreDictionary.getOres("blockOsmium"))
		{
			if(ore.isItemEqual(itemstack))
			{
				amount = 1800;
				break;
			}
		}
		
		return amount;
	}
}
