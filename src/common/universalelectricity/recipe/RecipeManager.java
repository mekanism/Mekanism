package universalelectricity.recipe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.src.Block;
import net.minecraft.src.CraftingManager;
import net.minecraft.src.FurnaceRecipes;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import universalelectricity.UEConfig;

/**
 * Adds recipes with full Forge ore dictionary support and replaceable by add-ons.
 * Use UERecipes.addRecipe(....) like how you use ModLoader.addRecipe.
 * 
 * MAKE SURE YOU ADD/REPLACE YOUR RECIPE BEFORE @PostInit OR IT WONT BE REPLACABLE!
 * @author Calclavia
 *
 */
public class RecipeManager
{
	//Crafting Recipes
	private static final List<CraftingRecipe> SHAPED_RECIPES = new ArrayList<CraftingRecipe>();
	private static final List<CraftingRecipe> SHAPELESS_RECIPES = new ArrayList<CraftingRecipe>();
	
    //Smelting Recipes
	private static final List<SmeltingRecipe> SMELTING_RECIPES = new ArrayList<SmeltingRecipe>();

    //Custom recipe handlers for UE machines
	private static final Map<String, IRecipeHandler> RECIPE_HANDLERS = new HashMap<String, IRecipeHandler>();
    
    //Shaped Recipes
    public static void addRecipe(ItemStack output, Object[] input)
    {
        SHAPED_RECIPES.add(new CraftingRecipe(output, input));
    }

    public static void addRecipe(Item output, Object[] input)
    {
        addRecipe(new ItemStack(output), input);
    }

    public static void addRecipe(Block output, Object[] input)
    {
        addRecipe(new ItemStack(output), input);
    }
    
    /**
     * Use this function if you want to check if the recipe is allowed in the configuration file.
     */
    public static void addRecipe(ItemStack output, Object[] input, String name, Configuration config, boolean defaultBoolean)
    {
    	if(config != null)
    	{
    		if(UEConfig.getConfigData(config, "Allow "+name+" Crafting", defaultBoolean))
    		{
    			addRecipe(output, input);
    		}
    	}
    }
    
    public static void addRecipe(ItemStack output, Object[] input, Configuration config, boolean defaultBoolean)
    {
    	addRecipe(output, input, output.getItemName(), config, defaultBoolean);
    }
    
    public static List<CraftingRecipe> getRecipes() { return SHAPED_RECIPES; }
    
    public static CraftingRecipe getRecipeByOutput(ItemStack output)
    {
    	for(CraftingRecipe recipe : SHAPED_RECIPES)
        {
            if(recipe.output == output)
            {
            	return recipe;
            }
        }
		return null;
    }

    //Shapeless Recipes
    public static void addShapelessRecipe(ItemStack output, Object[] input)
    {
        SHAPELESS_RECIPES.add(new CraftingRecipe(output, input));
    }

    public static void addShapelessRecipe(Item output, Object[] input)
    {
        addShapelessRecipe(new ItemStack(output), input);
    }

    public static void addShapelessRecipe(Block output, Object[] input)
    {
        addShapelessRecipe(new ItemStack(output), input);
    }
    
    /**
     * Use this function if you want to check if the recipe is allowed in the configuration file.
     */
    public static void addShapelessRecipe(ItemStack output, Object[] input, String name, Configuration config, boolean defaultBoolean)
    {
    	if(config != null)
    	{
    		if(UEConfig.getConfigData(config, "Allow "+name+" Crafting", defaultBoolean))
    		{
    			addShapelessRecipe(output, input);
    		}
    	}
    }
    public static void addShapelessRecipe(ItemStack output, Object[] input, Configuration config, boolean defaultBoolean)
    {
    	addShapelessRecipe(output, input, output.getItemName(), config, defaultBoolean);
    }
    
    public static List<CraftingRecipe> getShapelessRecipes() { return SHAPELESS_RECIPES; }
    
    public static CraftingRecipe getShapelessRecipeByOutput(ItemStack output)
    {
    	for(CraftingRecipe recipe : SHAPELESS_RECIPES)
        {
            if(recipe.output == output)
            {
            	return recipe;
            }
        }
		return null;
    }

    //Furnace Smelting Recipes
    public static void addSmelting(ItemStack input, ItemStack output)
    {
        SMELTING_RECIPES.add(new SmeltingRecipe(input, output));
    }

    public static void addSmelting(Item input, ItemStack output)
    {
        addSmelting(new ItemStack(input), output);
    }
    
    public static void addSmelting(Block input, ItemStack output)
    {
        addSmelting(new ItemStack(input), output);
    }
    
    /**
     * Use this function if you want to check if the recipe is allowed in the configuration file.
     */
    public static void addSmelting(ItemStack input, ItemStack output, String name, Configuration config, boolean defaultBoolean)
    {
    	if(config != null)
    	{
    		if(UEConfig.getConfigData(config, "Allow "+name+" Smelting", defaultBoolean))
    		{
    			addSmelting(input, output);
    		}
    	}
    }
    
    public static void addSmelting(ItemStack input, ItemStack output, Configuration config, boolean defaultBoolean)
    {
    	addSmelting(input, output, output.getItemName(), config, defaultBoolean);
    }
  
    
    public static List<SmeltingRecipe> getSmeltingRecipes() { return SMELTING_RECIPES; }
    
    public static SmeltingRecipe getSmeltingRecipeByOutput(ItemStack output)
    {
    	for(SmeltingRecipe recipe : SMELTING_RECIPES)
        {
            if(recipe.output == output)
            {
            	return recipe;
            }
        }
		return null;
    }
    /**
     * Registers your {@link #IRecipeHandler} to the Recipe Manager so other UE mods can access and modify your recipes.
     * @param handlerName - The name of your recipe handler. Make it something unique. This String is what other mods
     * will be using to access your recipe handler.
     * @param handler - An instance of your IRecipeHandler
     */
    public static void registerRecipeHandler(String handlerName, IRecipeHandler handler)
    {
    	if(!RECIPE_HANDLERS.containsKey(handlerName))
    	{
    		RECIPE_HANDLERS.put(handlerName, handler);
    	}
    }
    
    public static IRecipeHandler getRecipeHandler(String name)
    {
    	return RECIPE_HANDLERS.get(name);
    }
    
    public static Map<String, IRecipeHandler> getAllCustomRecipes() { return RECIPE_HANDLERS; }

    /**
     * Replacement functions must be called before post mod initialization!
     */
    public static void replaceRecipe(CraftingRecipe recipeToReplace, CraftingRecipe newRecipe)
    {
    	for(CraftingRecipe recipe : SHAPED_RECIPES)
        {
            if(recipe.isEqual(recipeToReplace))
            {
            	recipe = newRecipe;
            }
        }
    }
    
   
    public static void replaceShapelessRecipe(CraftingRecipe recipeToReplace, CraftingRecipe newRecipe)
    {
    	for(CraftingRecipe recipe : SHAPELESS_RECIPES)
        {
            if(recipe.isEqual(recipeToReplace))
            {
            	recipe = newRecipe;
            }
        }
    }
    
    public static void replaceSmeltingRecipe(SmeltingRecipe recipeToReplace, SmeltingRecipe newRecipe)
    {
        for(SmeltingRecipe recipe : SMELTING_RECIPES)
        {
            if(recipe.isEqual(recipeToReplace))
            {
            	recipe = newRecipe;
            }
        }
    }
    
    /**
     * Finds and returns all recipes that have this specific output
     * @param output - The output of the recipe.
     */
    public static List<CraftingRecipe> findRecipe(ItemStack output)
    {
    	List<CraftingRecipe> returnList = new ArrayList<CraftingRecipe>();
    	
    	for(CraftingRecipe recipe : SHAPED_RECIPES)
        {
            if(recipe.output.isItemEqual(output))
            {
            	returnList.add(recipe);
            }
        }
    	
    	return returnList;
    }
    
    public static List<CraftingRecipe> findShapelessRecipe(ItemStack output)
    {
    	List<CraftingRecipe> returnList = new ArrayList<CraftingRecipe>();
    	
    	for(CraftingRecipe recipe : SHAPELESS_RECIPES)
        {
            if(recipe.output.isItemEqual(output))
            {
            	returnList.add(recipe);
            }
        }
    	
    	return returnList;
    }
    
    public static List<SmeltingRecipe> findSmeltingRecipe(ItemStack output)
    {
    	List<SmeltingRecipe> returnList = new ArrayList<SmeltingRecipe>();
    	
    	for(SmeltingRecipe recipe : SMELTING_RECIPES)
        {
            if(recipe.output.isItemEqual(output))
            {
            	returnList.add(recipe);
            }
        }
    	
    	return returnList;
    }
    
    /**
     * Removes all recipes with the specific output.
     */
    public static void removeRecipe(ItemStack output)
    {    	
    	for(CraftingRecipe recipe : SHAPED_RECIPES)
        {
            if(recipe.output.isItemEqual(output))
            {
            	SHAPED_RECIPES.remove(recipe);
            }
        }
    }
    
    public static void removeShapelessRecipe(ItemStack output)
    {    	
    	for(CraftingRecipe recipe : SHAPELESS_RECIPES)
        {
            if(recipe.output.isItemEqual(output))
            {
            	SHAPELESS_RECIPES.remove(recipe);
            }
        }
    }
    
    public static void removeSmeltingRecipe(ItemStack output)
    {    	
    	for(SmeltingRecipe recipe : SMELTING_RECIPES)
        {
            if(recipe.output.isItemEqual(output))
            {
            	SMELTING_RECIPES.remove(recipe);
            }
        }
    }
    
    /**
     * Removes a specific recipe from the list.
     */
    public static void removeRecipe(CraftingRecipe output)
    {    	
    	for(CraftingRecipe recipe : SHAPED_RECIPES)
        {
            if(recipe.isEqual(output))
            {
            	SHAPED_RECIPES.remove(recipe);
            	return;
            }
        }
    }
    
    public static void removeShapelessRecipe(CraftingRecipe output)
    {    	
    	for(CraftingRecipe recipe : SHAPELESS_RECIPES)
        {
            if(recipe.isEqual(output))
            {
            	SHAPELESS_RECIPES.remove(recipe);
            	return;
            }
        }
    }
    
    public static void removeSmeltingRecipe(SmeltingRecipe output)
    {    	
    	for(SmeltingRecipe recipe : SMELTING_RECIPES)
        {
            if(recipe.isEqual(output))
            {
            	SMELTING_RECIPES.remove(recipe);
            	return;
            }
        }
    }
    
    /**
     * Called in post init by {@link #BasicComponenets} to add all recipes. Don't call this function.
     */
    public static void addRecipes()
    {
        for (CraftingRecipe recipe : SHAPED_RECIPES)
        {
            CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(recipe.output, recipe.input));
        }

        for (CraftingRecipe recipe : SHAPELESS_RECIPES)
        {
            CraftingManager.getInstance().getRecipeList().add(new ShapelessOreRecipe(recipe.output, recipe.input));
        }

        for (SmeltingRecipe recipe : SMELTING_RECIPES)
        {
            FurnaceRecipes.smelting().addSmelting(recipe.input.itemID, recipe.input.getItemDamage(), recipe.output);
        }
    }
}
