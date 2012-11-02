package net.uberkat.obsidian.common;

import java.util.*;
import net.minecraft.src.*;
import net.uberkat.obsidian.hawk.common.TileEntityWasher;

/** 
 * Class used to handle machine recipes. This is used for both adding recipes and checking outputs.
 * @author AidanBrady
 *
 */
public final class RecipeHandler
{
	/**
	 * Add an Enrichment Chamber recipe.
	 * @param input - input ItemStack
	 * @param output - output ItemStack
	 */
	public static void addEnrichmentChamberRecipe(ItemStack input, ItemStack output)
	{
		TileEntityEnrichmentChamber.recipes.add(new AbstractMap.SimpleEntry(input, output));
	}
	
	/**
	 * Add a Platinum Compressor recipe.
	 * @param input - input ItemStack
	 * @param output - output ItemStack
	 */
	public static void addPlatinumCompressorRecipe(ItemStack input, ItemStack output)
	{
		TileEntityPlatinumCompressor.recipes.add(new AbstractMap.SimpleEntry(input, output));
	}
	
	/**
	 * Add a Combiner recipe.
	 * @param input - input ItemStack
	 * @param output - output ItemStack
	 */
	public static void addCombinerRecipe(ItemStack input, ItemStack output)
	{
		TileEntityCombiner.recipes.add(new AbstractMap.SimpleEntry(input, output));
	}
	
	/**
	 * Add a Crusher recipe.
	 * @param input - input ItemStack
	 * @param output - output ItemStack
	 */
	public static void addCrusherRecipe(ItemStack input, ItemStack output)
	{
		TileEntityCrusher.recipes.add(new AbstractMap.SimpleEntry(input, output));
	}
	
	/**
	 * Add a Theoretical Elementizer recipe.
	 * @param input - input ItemStack
	 * @param output - output ItemStack
	 */
	public static void addTheoreticalElementizerRecipe(ItemStack input, ItemStack output)
	{
		TileEntityTheoreticalElementizer.recipes.add(new AbstractMap.SimpleEntry(input, output));
	}
	
	/**
	 * Gets the output ItemStack of the ItemStack in the parameters.
	 * @param itemstack - input ItemStack
	 * @param flag - whether or not to decrease stack size
	 * @param recipes - List object of recipes
	 * @return output ItemStack
	 */
	public static ItemStack getOutput(ItemStack itemstack, boolean flag, List recipes)
	{
		for(Iterator iterator = recipes.iterator(); iterator.hasNext();)
		{
			Map.Entry entry = (Map.Entry)iterator.next();
			
			if(((ItemStack)entry.getKey()).isItemEqual(itemstack) && itemstack.stackSize >= ((ItemStack)entry.getKey()).stackSize)
			{
				if (flag)
                {
                    itemstack.stackSize -= ((ItemStack)entry.getKey()).stackSize;
                }
				
				return ((ItemStack)entry.getValue()).copy();
			}
		}
		return null;
	}
}
