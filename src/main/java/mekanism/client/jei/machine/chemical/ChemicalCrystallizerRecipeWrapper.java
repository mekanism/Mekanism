package mekanism.client.jei.machine.chemical;

import mekanism.api.gas.GasStack;
import mekanism.client.jei.MekanismJEI;
import mekanism.common.recipe.machines.CrystallizerRecipe;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.item.ItemStack;

public class ChemicalCrystallizerRecipeWrapper implements IRecipeWrapper
{
	private final CrystallizerRecipe recipe;
	
	public ChemicalCrystallizerRecipeWrapper(CrystallizerRecipe r)
	{
		recipe = r;
	}
	
	@Override
	public void getIngredients(IIngredients ingredients) 
	{
		ingredients.setInput(MekanismJEI.GAS_INGREDIENT_TYPE, recipe.recipeInput.ingredient);
		ingredients.setOutput(VanillaTypes.ITEM, recipe.recipeOutput.output);
	}

	public CrystallizerRecipe getRecipe()
	{
		return recipe;
	}
}
