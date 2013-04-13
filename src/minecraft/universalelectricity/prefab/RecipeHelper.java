package universalelectricity.prefab;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.common.Configuration;
import cpw.mods.fml.common.registry.GameRegistry;

/**
 * This class is used to replace recipes that are already added in the existing recipe pool for
 * crafting and smelting. All recipe functions take account of the Forge Ore Dictionary. It also
 * includes some recipe helper functions to shorten some of your function calls.
 * 
 * @author Calclavia
 * 
 */
public class RecipeHelper
{
	public static List<IRecipe> getRecipesByOutput(ItemStack output)
	{
		List<IRecipe> list = new ArrayList<IRecipe>();

		for (Object obj : CraftingManager.getInstance().getRecipeList())
		{
			if (obj instanceof IRecipe)
			{
				if (((IRecipe) obj).getRecipeOutput() == output)
				{
					list.add((IRecipe) obj);
				}
			}
		}

		return list;
	}

	/**
	 * Replaces a recipe with a new IRecipe.
	 * 
	 * @return True if successful
	 */
	public static boolean replaceRecipe(IRecipe recipe, IRecipe newRecipe)
	{
		for (Object obj : CraftingManager.getInstance().getRecipeList())
		{
			if (obj instanceof IRecipe)
			{
				if (((IRecipe) obj).equals(recipe) || obj == recipe)
				{
					CraftingManager.getInstance().getRecipeList().remove(obj);
					CraftingManager.getInstance().getRecipeList().add(newRecipe);
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Replaces a recipe with the resulting ItemStack with a new IRecipe.
	 * 
	 * @return True if successful
	 */
	public static boolean replaceRecipe(ItemStack recipe, IRecipe newRecipe)
	{
		if (removeRecipe(recipe))
		{
			CraftingManager.getInstance().getRecipeList().add(newRecipe);
			return true;
		}

		return false;
	}

	/**
	 * Removes a recipe by its IRecipe class.
	 * 
	 * @return True if successful
	 */
	public static boolean removeRecipe(IRecipe recipe)
	{
		for (Object obj : CraftingManager.getInstance().getRecipeList())
		{
			if (obj != null)
			{
				if (obj instanceof IRecipe)
				{
					if (((IRecipe) obj).equals(recipe) || obj == recipe)
					{
						CraftingManager.getInstance().getRecipeList().remove(obj);
						return true;
					}
				}
			}
		}

		return false;
	}

	/**
	 * Removes the first recipe found by its output.
	 * 
	 * @return True if successful
	 */
	public static boolean removeRecipe(ItemStack stack)
	{
		for (Object obj : CraftingManager.getInstance().getRecipeList())
		{
			if (obj != null)
			{
				if (obj instanceof IRecipe)
				{
					if (((IRecipe) obj).getRecipeOutput() != null)
					{
						if (((IRecipe) obj).getRecipeOutput().isItemEqual(stack))
						{
							CraftingManager.getInstance().getRecipeList().remove(obj);
							return true;
						}
					}
				}
			}
		}

		return false;
	}

	/**
	 * Removes all recipes found that has this output. You may use this with Forge Ore Dictionary to
	 * remove all recipes with the FoD ID.
	 * 
	 * @return True if successful
	 */
	public static boolean removeRecipes(ItemStack... itemStacks)
	{
		boolean didRemove = false;

		for (Iterator itr = CraftingManager.getInstance().getRecipeList().iterator(); itr.hasNext();)
		{
			Object obj = itr.next();

			if (obj != null)
			{
				if (obj instanceof IRecipe)
				{
					if (((IRecipe) obj).getRecipeOutput() != null)
					{
						for (ItemStack itemStack : itemStacks)
						{
							if (((IRecipe) obj).getRecipeOutput().isItemEqual(itemStack))
							{
								itr.remove();
								didRemove = true;
								break;
							}
						}
					}
				}
			}
		}

		return didRemove;
	}

	/**
	 * Use this function if you want to check if the recipe is allowed in the configuration file.
	 */
	public static void addRecipe(IRecipe recipe, String name, Configuration configuration, boolean defaultBoolean)
	{
		if (configuration != null)
		{
			configuration.load();

			if (configuration.get("Crafting", "Allow " + name + " Crafting", defaultBoolean).getBoolean(defaultBoolean))
			{
				GameRegistry.addRecipe(recipe);
			}

			configuration.save();
		}
	}

	public static void addRecipe(IRecipe recipe, Configuration config, boolean defaultBoolean)
	{
		addRecipe(recipe, recipe.getRecipeOutput().getItemName(), config, defaultBoolean);
	}
}
