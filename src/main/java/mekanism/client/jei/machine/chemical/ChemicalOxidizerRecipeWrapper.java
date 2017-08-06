package mekanism.client.jei.machine.chemical;

import mekanism.api.gas.GasStack;
import mekanism.client.jei.machine.BaseRecipeWrapper;
import mekanism.common.recipe.machines.OxidationRecipe;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.item.ItemStack;

public class ChemicalOxidizerRecipeWrapper extends BaseRecipeWrapper
{
	public OxidationRecipe recipe;
	
	public ChemicalOxidizerRecipeCategory category;
	
	public ChemicalOxidizerRecipeWrapper(OxidationRecipe r, ChemicalOxidizerRecipeCategory c)
	{
		recipe = r;
		category = c;
	}
	
	@Override
	public void getIngredients(IIngredients ingredients) 
	{
		ingredients.setInput(ItemStack.class, recipe.recipeInput.ingredient);
		ingredients.setOutput(GasStack.class, recipe.recipeOutput.output);
	}
	
	@Override
	public ChemicalOxidizerRecipeCategory getCategory()
	{
		return category;
	}
}
