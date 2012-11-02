
package hawk.api;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import net.minecraft.src.Block;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraftforge.oredict.OreDictionary;


/**
 * 
 * 
 * 
 * @author Elusivehawk
 */
public class ProcessingRecipes
{
	private static Map washerSecondaries = new HashMap();
	private static Map washerRarities = new HashMap();
	
	private static Map quantityMapping = new HashMap();
	
	/**
	 * 
	 * Instead of IDs, processing now uses enums.
	 * 
	 * @author Elusivehawk
	 */
	public enum EnumProcessing
	{
		CRUSHING_EXPLOSIVES(new HashMap()),
		
		WASHING(new HashMap()),
		
		STAR_FORGE(new HashMap()),
		
		ENDIUM_FORGE(new HashMap()),
		
		HM_E2MM(new HashMap());
		
		private Map processingList;
		
		EnumProcessing(Map recipeList)
		{
			processingList = recipeList;
		}
		
		public Map getRecipeList()
		{
			return processingList;
		}
		
	}
	
	/**
	 * 
	 * Adds a processing recipe.
	 * 
	 * @param input The input.
	 * @param output The output.
	 * @param processType What type of processing this recipe is for.
	 */
	public static void addHMProcessingRecipe(ItemStack input, ItemStack output, EnumProcessing processType)
	{
		processType.getRecipeList().put(Arrays.asList(input.getItem(), input.isItemStackDamageable() ? 0 : input.getItemDamage(), input.isItemEnchanted(), input.stackTagCompound != null), output);
		quantityMapping.put(Arrays.asList(input.getItem(), input.getItemDamage(), processType), (Integer)input.stackSize);
		
	}
	
	public static void addHMProcessingRecipe(Item input, ItemStack output, EnumProcessing processType)
	{
		addHMProcessingRecipe(new ItemStack(input), output, processType);
	}
	
	public static void addHMProcessingRecipe(Item input, Item output, EnumProcessing processType)
	{
		addHMProcessingRecipe(new ItemStack(input), new ItemStack(output), processType);
	}
	
	public static void addHMProcessingRecipe(Item input, Block output, EnumProcessing processType)
	{
		addHMProcessingRecipe(new ItemStack(input), new ItemStack(output), processType);
	}
	
	public static void addHMProcessingRecipe(Block input, ItemStack output, EnumProcessing processType)
	{
		addHMProcessingRecipe(new ItemStack(input), output, processType);
	}
	
	public static void addHMProcessingRecipe(Block input, Item output, EnumProcessing processType)
	{
		addHMProcessingRecipe(new ItemStack(input), new ItemStack(output), processType);
	}
	
	public static void addHMProcessingRecipe(Block input, Block output, EnumProcessing processType)
	{
		addHMProcessingRecipe(new ItemStack(input), new ItemStack(output), processType);
	}
	
	/**
	 * 
	 * Forge Ore Dictionary supported version.
	 * 
	 * @param input
	 * @param output
	 * @param processingType
	 */
	public static void addHMFoDProcessingRecipe(String input, ItemStack output, EnumProcessing processType)
	{
		for (ItemStack ore : OreDictionary.getOres(input))
		{
			if (ore != null)
			{
				addHMProcessingRecipe(ore, output, processType);
			}
			
		}
		
	}
	
	public static void addHMWashingSecondary(ItemStack input, Object output, boolean isCommon)
	{
		if (isCommon)
		{
			washerSecondaries.put(Arrays.asList(input.getItem(), input.getItemDamage()), output);
		}
		else
		{
			washerRarities.put(Arrays.asList(input.getItem(), input.getItemDamage()), output);
		}
		
	}
	
	public static ItemStack getResult(ItemStack input, EnumProcessing processType)
	{
		if (input == null)
		{
			return null;
		}
		else
		{
			ItemStack output = (ItemStack)processType.getRecipeList().get(Arrays.asList(input.getItem(), input.isItemStackDamageable() ? 0 : input.getItemDamage(), input.isItemEnchanted(), input.stackTagCompound != null));
			
			if (output != null)
			{
				if (input.isItemEqual(output) && input.isItemStackDamageable() && output.isItemStackDamageable())
				{
					output.setItemDamage(input.getItemDamage());
				}
			}
			return output;
		}
	}
	
	public static int getQuantity(ItemStack item, EnumProcessing processType)
	{
		return (Integer)quantityMapping.get(Arrays.asList(item.getItem(), item.getItemDamage(), processType));
	}
	
	public static ItemStack getWashingSecondaryResult(ItemStack item, Random random)
	{
		int secondaryChance = random.nextInt(100);
		
		if (item == null)
		{
			return null;
		}
		else
		{
			if (secondaryChance < 5)
			{
				ItemStack ret = (ItemStack)washerRarities.get(Arrays.asList(item.getItem(), item.getItemDamage()));
				
				if (ret != null) 
				{
					return ret;
				}
				
			}
			else if (secondaryChance < 10)
			{
				ItemStack ret = (ItemStack)washerSecondaries.get(Arrays.asList(item.getItem(), item.getItemDamage()));
				
				if (ret != null) 
				{
					return ret;
				}
				
			}
			
			return null;
		}
		
	}
	
}
