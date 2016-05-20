package mekanism.client.jei.crafting;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import mekanism.common.recipe.ShapedMekanismRecipe;
import mezz.jei.api.recipe.BlankRecipeWrapper;
import mezz.jei.api.recipe.wrapper.IShapedCraftingRecipeWrapper;
import net.minecraft.item.ItemStack;

public class ShapedMekanismRecipeWrapper extends BlankRecipeWrapper implements IShapedCraftingRecipeWrapper
{
	@Nonnull
	private final ShapedMekanismRecipe recipe;
	
	public ShapedMekanismRecipeWrapper(@Nonnull ShapedMekanismRecipe r) 
	{
		recipe = r;
		
		for(Object input : this.recipe.getInput()) 
		{
			if(input instanceof ItemStack) 
			{
				ItemStack itemStack = (ItemStack)input;
				
				if(itemStack.stackSize != 1) 
				{
					itemStack.stackSize = 1;
				}
			}
		}
	}

	@Nonnull
	@Override
	public List getInputs()
	{
		return Arrays.asList(recipe.getInput());
	}

	@Nonnull
	@Override
	public List<ItemStack> getOutputs() 
	{
		return Collections.singletonList(recipe.getRecipeOutput());
	}

	@Override
	public int getWidth() 
	{
		return recipe.width;
	}

	@Override
	public int getHeight()
	{
		return recipe.height;
	}
}
