package mekanism.client.jei.machine.chemical;

import mekanism.api.gas.GasStack;
import mekanism.client.jei.MekanismJEI;
import mekanism.common.MekanismFluids;
import mekanism.common.recipe.machines.DissolutionRecipe;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.item.ItemStack;

public class ChemicalDissolutionChamberRecipeWrapper implements IRecipeWrapper
{
	private final DissolutionRecipe recipe;
	
	public ChemicalDissolutionChamberRecipeWrapper(DissolutionRecipe r)
	{
		recipe = r;
	}
	
	@Override
	public void getIngredients(IIngredients ingredients) 
	{
		ingredients.setInput(MekanismJEI.GAS_INGREDIENT_TYPE, new GasStack(MekanismFluids.SulfuricAcid, 1000));
		ingredients.setInput(VanillaTypes.ITEM, recipe.recipeInput.ingredient);
		ingredients.setOutput(MekanismJEI.GAS_INGREDIENT_TYPE, recipe.recipeOutput.output);
	}

	public DissolutionRecipe getRecipe()
	{
		return recipe;
	}
}
