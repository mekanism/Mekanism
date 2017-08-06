package mekanism.client.jei.machine.other;

import mekanism.api.gas.GasStack;
import mekanism.client.jei.machine.BaseRecipeWrapper;
import mekanism.common.recipe.machines.PressurizedRecipe;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public class PRCRecipeWrapper extends BaseRecipeWrapper
{
	public PressurizedRecipe recipe;
	
	public PRCRecipeCategory category;
	
	public PRCRecipeWrapper(PressurizedRecipe r, PRCRecipeCategory c)
	{
		recipe = r;
		category = c;
	}
	
	@Override
	public void getIngredients(IIngredients ingredients) 
	{
		ingredients.setInput(ItemStack.class, recipe.recipeInput.getSolid());
		ingredients.setInput(FluidStack.class, recipe.recipeInput.getFluid());
		ingredients.setInput(GasStack.class, recipe.recipeInput.getGas());
		ingredients.setOutput(ItemStack.class, recipe.recipeOutput.getItemOutput());
		ingredients.setOutput(GasStack.class, recipe.recipeOutput.getGasOutput());
	}
	
	@Override
	public PRCRecipeCategory getCategory()
	{
		return category;
	}
}
