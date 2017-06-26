package mekanism.client.jei.machine.other;

import mekanism.api.gas.GasStack;
import mekanism.client.jei.machine.BaseRecipeWrapper;
import mekanism.common.recipe.machines.PressurizedRecipe;
import mekanism.common.util.LangUtils;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

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
	
	@Nullable
	@Override
	public List<String> getTooltipStrings(int mouseX, int mouseY)
	{
		List<String> currenttip = new ArrayList<String>();
		
		if(mouseX >= 29-3 && mouseX <= 45-3 && mouseY >= 11-12 && mouseY <= 69-12)
		{
			currenttip.add(LangUtils.localizeGasStack(recipe.getInput().getGas()));
		}
		else if(mouseX >= 141-3 && mouseX <= 157-3 && mouseY >= 41-12 && mouseY <= 69-12)
		{
			currenttip.add(LangUtils.localizeGasStack(recipe.getOutput().getGasOutput()));
		}
		
		return currenttip;
	}
	
	@Override
	public PRCRecipeCategory getCategory()
	{
		return category;
	}
}
