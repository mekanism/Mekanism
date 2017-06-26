package mekanism.client.jei.machine.chemical;

import mekanism.api.gas.GasStack;
import mekanism.client.jei.machine.BaseRecipeWrapper;
import mekanism.common.recipe.machines.CrystallizerRecipe;
import mekanism.common.util.LangUtils;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

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
	
	@Nullable
	@Override
	public List<String> getTooltipStrings(int mouseX, int mouseY)
	{
		List<String> currenttip = new ArrayList<String>();
		
		if(mouseX >= 1 && mouseX <= 17 && mouseY >= 5-3 && mouseY <= 63-3)
		{
			currenttip.add(LangUtils.localizeGasStack(recipe.getInput().ingredient));
		}
		
		return currenttip;
	}
	
	@Override
	public ChemicalCrystallizerRecipeCategory getCategory()
	{
		return category;
	}
}
