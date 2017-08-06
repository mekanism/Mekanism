package mekanism.client.jei.machine.chemical;

import mekanism.api.gas.GasStack;
import mekanism.client.jei.machine.BaseRecipeWrapper;
import mekanism.common.MekanismFluids;
import mekanism.common.recipe.machines.DissolutionRecipe;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.item.ItemStack;

public class ChemicalDissolutionChamberRecipeWrapper extends BaseRecipeWrapper
{
	public DissolutionRecipe recipe;
	
	public ChemicalDissolutionChamberRecipeCategory category;
	
	public ChemicalDissolutionChamberRecipeWrapper(DissolutionRecipe r, ChemicalDissolutionChamberRecipeCategory c)
	{
		recipe = r;
		category = c;
	}
	
	@Override
	public void getIngredients(IIngredients ingredients) 
	{
		ingredients.setInput(GasStack.class, new GasStack(MekanismFluids.SulfuricAcid, 1000));
		ingredients.setInput(ItemStack.class, recipe.recipeInput.ingredient);
		ingredients.setOutput(GasStack.class, recipe.recipeOutput.output);
	}
	
	@Override
	public ChemicalDissolutionChamberRecipeCategory getCategory()
	{
		return category;
	}
}
