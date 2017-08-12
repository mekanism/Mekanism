package mekanism.client.jei.machine.chemical;

import mekanism.api.gas.GasStack;
import mekanism.client.jei.machine.BaseRecipeWrapper;
import mekanism.common.recipe.machines.OxidationRecipe;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.item.ItemStack;

public class ChemicalOxidizerRecipeWrapper implements IRecipeWrapper
{
	public OxidationRecipe recipe;
	
	public ChemicalOxidizerRecipeCategory category;
	
	public ChemicalOxidizerRecipeWrapper(OxidationRecipe r)
	{
		recipe = r;
	}
	
	@Override
	public void getIngredients(IIngredients ingredients) 
	{
		ingredients.setInput(ItemStack.class, recipe.recipeInput.ingredient);
		ingredients.setOutput(GasStack.class, recipe.recipeOutput.output);
	}
}
