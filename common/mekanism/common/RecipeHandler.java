package mekanism.common;

import java.util.HashMap;
import java.util.Map;

import mekanism.api.infuse.InfusionInput;
import mekanism.api.infuse.InfusionOutput;
import net.minecraft.item.ItemStack;

/** 
 * Class used to handle machine recipes. This is used for both adding recipes and checking outputs.
 * @author AidanBrady
 *
 */
public final class RecipeHandler
{	
	public static void addRecipe(Recipe recipe, Object input, Object output)
	{
		recipe.put(input, output);
	}
	
	/**
	 * Add an Enrichment Chamber recipe.
	 * @param input - input ItemStack
	 * @param output - output ItemStack
	 */
	public static void addEnrichmentChamberRecipe(ItemStack input, ItemStack output)
	{
		Recipe.ENRICHMENT_CHAMBER.put(input, output);
	}
	
	/**
	 * Add an Osmium Compressor recipe.
	 * @param input - input ItemStack
	 * @param output - output ItemStack
	 */
	public static void addOsmiumCompressorRecipe(ItemStack input, ItemStack output)
	{
		Recipe.OSMIUM_COMPRESSOR.put(input, output);
	}
	
	/**
	 * Add a Combiner recipe.
	 * @param input - input ItemStack
	 * @param output - output ItemStack
	 */
	public static void addCombinerRecipe(ItemStack input, ItemStack output)
	{
		Recipe.COMBINER.put(input, output);
	}
	
	/**
	 * Add a Crusher recipe.
	 * @param input - input ItemStack
	 * @param output - output ItemStack
	 */
	public static void addCrusherRecipe(ItemStack input, ItemStack output)
	{
		Recipe.CRUSHER.put(input, output);
	}
	
	/**
	 * Add a Purification Chamber recipe.
	 * @param input - input ItemStack
	 * @param output - output ItemStack
	 */
	public static void addPurificationChamberRecipe(ItemStack input, ItemStack output)
	{
		Recipe.PURIFICATION_CHAMBER.put(input, output);
	}
	
	/**
	 * Add a Metallurgic Infuser recipe.
	 * @param input - input Infusion
	 * @param output - output ItemStack
	 */
	public static void addMetallurgicInfuserRecipe(InfusionInput input, ItemStack output)
	{
		Recipe.METALLURGIC_INFUSER.put(input, InfusionOutput.getInfusion(input, output));
	}
	
	/**
	 * Gets the InfusionOutput of the InfusionInput in the parameters.
	 * @param infusion - input Infusion
	 * @param stackDecrease - whether or not to decrease the input slot's stack size AND the infuse amount
	 * @param recipes - Map of recipes
	 * @return InfusionOutput
	 */
	public static InfusionOutput getOutput(InfusionInput infusion, boolean stackDecrease, Map<InfusionInput, InfusionOutput> recipes)
	{
		if(infusion != null && infusion.inputStack != null)
		{
			for(Map.Entry entry : recipes.entrySet())
			{
				if(((InfusionInput)entry.getKey()).inputStack.isItemEqual(infusion.inputStack) && infusion.inputStack.stackSize >= ((InfusionInput)entry.getKey()).inputStack.stackSize)
				{
					if(infusion.infusionType == ((InfusionInput)entry.getKey()).infusionType)
					{
						if(stackDecrease)
						{
							infusion.inputStack.stackSize -= ((InfusionInput)entry.getKey()).inputStack.stackSize;
						}
						
						return ((InfusionOutput)entry.getValue()).copy();
					}
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
		if(itemstack != null)
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
		}
		
		return null;
	}
	
	public static enum Recipe
	{
		ENRICHMENT_CHAMBER(new HashMap<ItemStack, ItemStack>()),
		OSMIUM_COMPRESSOR(new HashMap<ItemStack, ItemStack>()),
		COMBINER(new HashMap<ItemStack, ItemStack>()),
		CRUSHER(new HashMap<ItemStack, ItemStack>()),
		PURIFICATION_CHAMBER(new HashMap<ItemStack, ItemStack>()),
		METALLURGIC_INFUSER(new HashMap<InfusionInput, InfusionOutput>());
		
		private HashMap recipes;
		
		private Recipe(HashMap map)
		{
			recipes = map;
		}
		
		public void put(Object input, Object output)
		{
			recipes.put(input, output);
		}
		
		public boolean containsRecipe(ItemStack input)
		{
			for(Object obj : get().entrySet())
			{
				if(obj instanceof Map.Entry)
				{
					Map.Entry entry = (Map.Entry)obj;
					
					if(entry.getKey() instanceof ItemStack)
					{
						if(((ItemStack)entry.getKey()).isItemEqual(input))
						{
							return true;
						}
					}
				}
			}
			
			return false;
		}
		
		public HashMap get()
		{
			return recipes;
		}
	}
}
