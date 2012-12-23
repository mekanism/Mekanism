package mekanism.common;

import java.util.Map;

import mekanism.api.Infusion;
import net.minecraft.item.ItemStack;

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
		TileEntityEnrichmentChamber.recipes.put(input, output);
	}
	
	/**
	 * Add a Platinum Compressor recipe.
	 * @param input - input ItemStack
	 * @param output - output ItemStack
	 */
	public static void addPlatinumCompressorRecipe(ItemStack input, ItemStack output)
	{
		TileEntityPlatinumCompressor.recipes.put(input, output);
	}
	
	/**
	 * Add a Combiner recipe.
	 * @param input - input ItemStack
	 * @param output - output ItemStack
	 */
	public static void addCombinerRecipe(ItemStack input, ItemStack output)
	{
		TileEntityCombiner.recipes.put(input, output);
	}
	
	/**
	 * Add a Crusher recipe.
	 * @param input - input ItemStack
	 * @param output - output ItemStack
	 */
	public static void addCrusherRecipe(ItemStack input, ItemStack output)
	{
		TileEntityCrusher.recipes.put(input, output);
	}
	
	/**
	 * Add a Theoretical Elementizer recipe.
	 * @param input - input ItemStack
	 * @param output - output ItemStack
	 */
	public static void addTheoreticalElementizerRecipe(ItemStack input, ItemStack output)
	{
		TileEntityTheoreticalElementizer.recipes.put(input, output);
	}
	
	/**
	 * Adds a Metallurgic Infuser recipe.
	 * @param input - input Infusion
	 * @param output - output ItemStack
	 */
	public static void addMetallurgicInfuserRecipe(Infusion input, ItemStack output)
	{
		TileEntityMetallurgicInfuser.recipes.put(input, output);
	}
	
	/**
	 * Gets the output ItemStack of the Infusion in the parameters.
	 * @param infusion - input Infusion
	 * @param stackDecrease - whether or not to decrease the input slot's stack size AND the infuse amount
	 * @param recipes - Map of recipes
	 * @return output ItemStack
	 */
	public static ItemStack getOutput(Infusion infusion, boolean stackDecrease, Map<Infusion, ItemStack> recipes)
	{
		for(Map.Entry entry : recipes.entrySet())
		{
			if(((Infusion)entry.getKey()).resource.isItemEqual(infusion.resource) && infusion.resource.stackSize >= ((Infusion)entry.getKey()).resource.stackSize)
			{
				if(infusion.type == ((Infusion)entry.getKey()).type)
				{
					if(stackDecrease)
					{
						infusion.resource.stackSize -= ((Infusion)entry.getKey()).resource.stackSize;
					}
					
					return ((ItemStack)entry.getValue()).copy();
				}
			}
		}
		
		return null;
	}
	
	/**
	 * Gets the output ItemStack of the ItemStack in the parameters.
	 * @param itemstack - input ItemStack
	 * @param stackDecrease - whether or not to decrease the input slot's stack size
	 * @param recipes - Map of recipes
	 * @return output ItemStack
	 */
	public static ItemStack getOutput(ItemStack itemstack, boolean stackDecrease, Map<ItemStack, ItemStack> recipes)
	{
		for(Map.Entry entry : recipes.entrySet())
		{
			if(((ItemStack)entry.getKey()).isItemEqual(itemstack) && itemstack.stackSize >= ((ItemStack)entry.getKey()).stackSize)
			{
				if(stackDecrease)
				{
					itemstack.stackSize -= ((ItemStack)entry.getKey()).stackSize;
				}
				
				return ((ItemStack)entry.getValue()).copy();
			}
		}
		
		return null;
	}
}
