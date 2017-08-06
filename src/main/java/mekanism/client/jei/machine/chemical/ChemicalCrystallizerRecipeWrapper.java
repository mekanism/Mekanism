package mekanism.client.jei.machine.chemical;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import mekanism.api.gas.GasStack;
import mekanism.client.jei.machine.BaseRecipeWrapper;
import mekanism.common.recipe.machines.CrystallizerRecipe;
import mekanism.common.util.LangUtils;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.item.ItemStack;

public class ChemicalCrystallizerRecipeWrapper extends BaseRecipeWrapper
{
	public CrystallizerRecipe recipe;
	
	public ChemicalCrystallizerRecipeCategory category;
	
	public ChemicalCrystallizerRecipeWrapper(CrystallizerRecipe r, ChemicalCrystallizerRecipeCategory c)
	{
		recipe = r;
		category = c;
	}
	
	@Override
	public void getIngredients(IIngredients ingredients) 
	{
		ingredients.setInput(GasStack.class, recipe.recipeInput.ingredient);
		ingredients.setOutput(ItemStack.class, recipe.recipeOutput.output);
	}
	
	@Override
	public ChemicalCrystallizerRecipeCategory getCategory()
	{
		return category;
	}
}
