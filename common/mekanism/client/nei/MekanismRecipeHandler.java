package mekanism.client.nei;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import mekanism.api.energy.IEnergizedItem;
import mekanism.common.IEnergyCube;
import mekanism.common.IFactory;
import mekanism.common.MekanismRecipe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.ShapedRecipeHandler;

public class MekanismRecipeHandler extends ShapedRecipeHandler
{
	@Override
	public String getRecipeName()
	{
		return "Shaped Mekanism Crafting";
	}
	
	@Override
	public void loadCraftingRecipes(String outputId, Object... results)
	{
		if(outputId.equals("crafting") && getClass() == MekanismRecipeHandler.class)
		{
			List<IRecipe> allrecipes = CraftingManager.getInstance().getRecipeList();
			
			for(IRecipe irecipe : allrecipes)
			{
				if(irecipe instanceof MekanismRecipe)
				{
					MekanismRecipe energyRecipe = (MekanismRecipe)irecipe;
					CachedEnergyRecipe recipe = new CachedEnergyRecipe(energyRecipe.width, energyRecipe.height, energyRecipe.getInput(), energyRecipe.getRecipeOutput());
					arecipes.add(recipe);
				}
			}
		}
		else {
			super.loadCraftingRecipes(outputId, results);
		}
	}
	
	@Override
	public void loadCraftingRecipes(ItemStack result)
	{
		List<IRecipe> allrecipes = CraftingManager.getInstance().getRecipeList();
		
		for(IRecipe irecipe : allrecipes)
		{
			if(irecipe instanceof MekanismRecipe && areItemsEqual(irecipe.getRecipeOutput(), result))
			{
				MekanismRecipe energyRecipe = (MekanismRecipe)irecipe;
				CachedEnergyRecipe recipe = new CachedEnergyRecipe(energyRecipe.width, energyRecipe.height, energyRecipe.getInput(), energyRecipe.getRecipeOutput());
				arecipes.add(recipe);
			}
		}
	}
	
	@Override
	public void loadUsageRecipes(ItemStack ingredient)
	{
		List<IRecipe> allrecipes = CraftingManager.getInstance().getRecipeList();
		
		for(IRecipe irecipe : allrecipes)
		{
			if(irecipe instanceof MekanismRecipe)
			{
				MekanismRecipe energyRecipe = (MekanismRecipe)irecipe;
				CachedEnergyRecipe recipe = new CachedEnergyRecipe(energyRecipe.width, energyRecipe.height, energyRecipe.getInput(), energyRecipe.getRecipeOutput());
				
				if(recipe.contains(recipe.ingredients, ingredient))
				{
					recipe.setIngredientPermutation(recipe.ingredients, ingredient);
					arecipes.add(recipe);
				}
			}
		}
	}
	
    public static boolean areItemsEqual(ItemStack stack1, ItemStack stack2)
    {
    	if(stack1 == null && stack2 != null || stack1 != null && stack2 == null)
    	{
    		return false;
    	}
    	else if(stack1 == null && stack2 == null)
    	{
    		return true;
    	}
    	
        if(stack1.itemID != stack2.itemID)
        {
        	return false;
        }
        
        if(!(stack1.getItem() instanceof IEnergizedItem) && !(stack2.getItem() instanceof IEnergizedItem))
        {
	        if(stack1.getItemDamage() != stack2.getItemDamage())
	        {
	        	return false;
	        }
        }
        else {
        	if(((IEnergizedItem)stack1.getItem()).isMetadataSpecific() && ((IEnergizedItem)stack2.getItem()).isMetadataSpecific())
        	{
        		if(stack1.getItemDamage() != stack2.getItemDamage())
        		{
        			return false;
        		}
        	}
        	
            if(stack1.getItem() instanceof IEnergyCube && stack2.getItem() instanceof IEnergyCube)
            {
            	if(((IEnergyCube)stack1.getItem()).getEnergyCubeTier(stack1) != ((IEnergyCube)stack2.getItem()).getEnergyCubeTier(stack2))
            	{
            		return false;
            	}
            }
            else if(stack1.getItem() instanceof IFactory && stack2.getItem() instanceof IFactory)
            {
            	if(((IFactory)stack1.getItem()).isFactory(stack1) && ((IFactory)stack2.getItem()).isFactory(stack2))
            	{
            		if(((IFactory)stack1.getItem()).getRecipeType(stack1) != ((IFactory)stack2.getItem()).getRecipeType(stack2))
            		{
            			return false;
            		}
            	}
            }
        }
    	
    	return true;
    }
	
	public class CachedEnergyRecipe extends CachedRecipe
	{
		public ArrayList<PositionedStack> ingredients;
		public PositionedStack result;
		
		public CachedEnergyRecipe(int width, int height, Object[] items, ItemStack out)
		{
			result = new PositionedStack(out, 119, 24);
			ingredients = new ArrayList<PositionedStack>();
			setIngredients(width, height, items);
		}
		
		public void setIngredients(int width, int height, Object[] items)
		{
			for(int x = 0; x < width; x++)
			{
				for(int y = 0; y < height; y++)
				{
					if(items[y*width+x] == null)
					{
						continue;
					}
					
					PositionedStack stack = new PositionedStack(items[y*width+x], 25+x*18, 6+y*18);
					stack.setMaxSize(1);
					ingredients.add(stack);
				}
			}
		}
		
		@Override
		public ArrayList<PositionedStack> getIngredients()
		{
			return (ArrayList<PositionedStack>)getCycledIngredients(MekanismRecipeHandler.this.cycleticks / 20, ingredients);
		}
		
		@Override
		public PositionedStack getResult()
		{
			return result;
		}
		
		@Override
		public boolean contains(Collection<PositionedStack> ingredients, ItemStack ingredient)
		{
			for(PositionedStack stack : ingredients)
			{
				for(ItemStack item : stack.items)
				{
					if(areItemsEqual(item, ingredient))
					{
						return true;
					}
				}
			}
			
			return false;
		}
	}
}
