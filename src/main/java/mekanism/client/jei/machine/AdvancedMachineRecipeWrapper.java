package mekanism.client.jei.machine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import mekanism.api.gas.Gas;
import mekanism.common.recipe.inputs.AdvancedMachineInput;
import mekanism.common.recipe.machines.AdvancedMachineRecipe;
import mekanism.common.recipe.outputs.ItemStackOutput;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.BlankRecipeWrapper;
import net.minecraft.item.ItemStack;

public abstract class AdvancedMachineRecipeWrapper extends BlankRecipeWrapper
{
	public AdvancedMachineRecipe recipe;
	
	public AdvancedMachineRecipeCategory category;
	
	public AdvancedMachineRecipeWrapper(AdvancedMachineRecipe r, AdvancedMachineRecipeCategory c)
	{
		recipe = r;
		category = c;
	}

	@Override
	public void getIngredients(IIngredients ingredients) 
	{
		ingredients.setInput(ItemStack.class, ((AdvancedMachineInput)recipe.getInput()).itemStack);
		ingredients.setOutput(ItemStack.class, ((ItemStackOutput)recipe.getOutput()).output);
	}
	
	@Nullable
	@Override
	public List<String> getTooltipStrings(int mouseX, int mouseY)
	{
		List<String> currenttip = new ArrayList<String>();
		
		if(mouseX >= 33 && mouseX <= 39 && mouseY >= 22 && mouseY <= 34)
		{
			currenttip.add(((AdvancedMachineInput)recipe.getInput()).gasType.getLocalizedName());
		}
		
		return currenttip;
	}
	
	public abstract List<ItemStack> getFuelStacks(Gas gasType);
}
