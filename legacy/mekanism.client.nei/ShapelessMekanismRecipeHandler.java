package mekanism.client.nei;

import java.util.ArrayList;
import java.util.List;

import mekanism.common.recipe.ShapelessMekanismRecipe;
import mekanism.common.util.LangUtils;
import mekanism.common.util.RecipeUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.ShapelessRecipeHandler;

public class ShapelessMekanismRecipeHandler extends ShapelessRecipeHandler
{
	@Override
	public String getRecipeName()
	{
		return "Mekanism " + LangUtils.localize("recipe.mekanismShapeless");
	}
	
	@Override
	public void loadCraftingRecipes(String outputId, Object... results)
	{
		if(outputId.equals("crafting") && getClass() == ShapelessMekanismRecipeHandler.class)
		{
			List<IRecipe> allrecipes = CraftingManager.getInstance().getRecipeList();

			for(IRecipe irecipe : allrecipes)
			{
				if(irecipe instanceof ShapelessMekanismRecipe)
				{
					ShapelessMekanismRecipe mekanismRecipe = (ShapelessMekanismRecipe)irecipe;
					CachedShapelessMekanismRecipe recipe = new CachedShapelessMekanismRecipe(mekanismRecipe);

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
			if(irecipe instanceof ShapelessMekanismRecipe && RecipeUtils.areItemsEqualForCrafting(irecipe.getRecipeOutput(), result))
			{
				ShapelessMekanismRecipe mekanismRecipe = (ShapelessMekanismRecipe)irecipe;
				CachedShapelessMekanismRecipe recipe = new CachedShapelessMekanismRecipe(mekanismRecipe);

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
			if(irecipe instanceof ShapelessMekanismRecipe)
			{
				ShapelessMekanismRecipe mekanismRecipe = (ShapelessMekanismRecipe)irecipe;
				CachedShapelessMekanismRecipe recipe = new CachedShapelessMekanismRecipe(mekanismRecipe);
				
				if(recipe.contains(recipe.ingredients, ingredient))
				{
					recipe.setIngredientPermutation(recipe.ingredients, ingredient);
					arecipes.add(recipe);
				}
			}
		}
	}
	
	public class CachedShapelessMekanismRecipe extends CachedRecipe
    {
        public ArrayList<PositionedStack> ingredients;
        public PositionedStack result;
        
        public CachedShapelessMekanismRecipe(ShapelessMekanismRecipe recipe) 
        {
        	result = new PositionedStack(recipe.getRecipeOutput(), 119, 24);
            ingredients = new ArrayList<PositionedStack>();
            setIngredients(recipe.getInput());
        }

        public void setIngredients(List<?> items) 
        {
            ingredients.clear();
            
            for(int x = 0; x < items.size(); x++) 
            {
                PositionedStack stack = new PositionedStack(items.get(x), 25 + stackorder[x][0] * 18, 6 + stackorder[x][1] * 18);
                stack.setMaxSize(1);
                ingredients.add(stack);
            }
        }

        public void setResult(ItemStack output) 
        {
            result = new PositionedStack(output, 119, 24);
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
    }
}
