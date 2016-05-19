package mekanism.client.jei;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import mekanism.common.recipe.ShapelessMekanismRecipe;
import mezz.jei.api.recipe.BlankRecipeWrapper;
import mezz.jei.api.recipe.wrapper.ICraftingRecipeWrapper;
import net.minecraft.item.ItemStack;

public class ShapelessMekanismRecipeWrapper extends BlankRecipeWrapper implements ICraftingRecipeWrapper
{
	@Nonnull
	private final ShapelessMekanismRecipe recipe;

	public ShapelessMekanismRecipeWrapper(@Nonnull ShapelessMekanismRecipe r)
	{
		recipe = r;
		
		for(Object input : recipe.getInput()) 
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
		return recipe.getInput();
	}

	@Nonnull
	@Override
	public List<ItemStack> getOutputs() 
	{
		return Collections.singletonList(recipe.getRecipeOutput());
	}
}
