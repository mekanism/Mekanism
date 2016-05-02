package mekanism.client.nei;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import mekanism.common.recipe.ShapedMekanismRecipe;
import mekanism.common.util.LangUtils;
import mekanism.common.util.RecipeUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.ShapedRecipeHandler;

public class ShapedMekanismRecipeHandler extends ShapedRecipeHandler
{
	@Override
	public String getRecipeName()
	{
		return "Mekanism " + LangUtils.localize("recipe.mekanismShaped");
	}

	@Override
	public void loadCraftingRecipes(String outputId, Object... results)
	{
		if(outputId.equals("crafting") && getClass() == ShapedMekanismRecipeHandler.class)
		{
			List<IRecipe> allrecipes = CraftingManager.getInstance().getRecipeList();

			for(IRecipe irecipe : allrecipes)
			{
				if(irecipe instanceof ShapedMekanismRecipe)
				{
					ShapedMekanismRecipe mekanismRecipe = (ShapedMekanismRecipe)irecipe;
					CachedShapedMekanismRecipe recipe = new CachedShapedMekanismRecipe(mekanismRecipe);
					
					recipe.computeVisuals();
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
			if(irecipe instanceof ShapedMekanismRecipe && RecipeUtils.areItemsEqualForCrafting(irecipe.getRecipeOutput(), result))
			{
				ShapedMekanismRecipe mekanismRecipe = (ShapedMekanismRecipe)irecipe;
				CachedShapedMekanismRecipe recipe = new CachedShapedMekanismRecipe(mekanismRecipe);
				
				recipe.computeVisuals();
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
			if(irecipe instanceof ShapedMekanismRecipe)
			{
				ShapedMekanismRecipe mekanismRecipe = (ShapedMekanismRecipe)irecipe;
				CachedShapedMekanismRecipe recipe = new CachedShapedMekanismRecipe(mekanismRecipe);

				recipe.computeVisuals();
				
				if(recipe.contains(recipe.ingredients, ingredient))
				{
					recipe.setIngredientPermutation(recipe.ingredients, ingredient);
					arecipes.add(recipe);
				}
			}
		}
	}

	public class CachedShapedMekanismRecipe extends CachedRecipe
	{
		public ArrayList<PositionedStack> ingredients;
		public PositionedStack result;

		public CachedShapedMekanismRecipe(ShapedMekanismRecipe recipe)
		{
			result = new PositionedStack(recipe.getRecipeOutput(), 119, 24);
			ingredients = new ArrayList<PositionedStack>();
			setIngredients(recipe.width, recipe.height, recipe.getInput());
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
		public List<PositionedStack> getIngredients()
		{
			return getCycledIngredients(cycleticks / 20, ingredients);
		}

		@Override
		public PositionedStack getResult()
		{
			return result;
		}
		
		public void computeVisuals() 
		{
            for(PositionedStack p : ingredients)
            {
                p.generatePermutations();
            }
        }

		@Override
		public boolean contains(Collection<PositionedStack> ingredients, ItemStack ingredient)
		{
			for(PositionedStack stack : ingredients)
			{
				for(ItemStack item : stack.items)
				{
					if(RecipeUtils.areItemsEqualForCrafting(item, ingredient))
					{
						return true;
					}
				}
			}

			return false;
		}
	}
}
