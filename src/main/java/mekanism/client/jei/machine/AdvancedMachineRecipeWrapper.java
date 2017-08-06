package mekanism.client.jei.machine;

import java.util.List;

import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.common.recipe.inputs.AdvancedMachineInput;
import mekanism.common.recipe.machines.AdvancedMachineRecipe;
import mekanism.common.recipe.outputs.ItemStackOutput;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.item.ItemStack;

public abstract class AdvancedMachineRecipeWrapper extends BaseRecipeWrapper
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
		ingredients.setInput(GasStack.class, new GasStack(((AdvancedMachineInput)recipe.getInput()).gasType, 1));
		ingredients.setOutput(ItemStack.class, ((ItemStackOutput)recipe.getOutput()).output);
	}
	
	@Override
	public AdvancedMachineRecipeCategory getCategory()
	{
		return category;
	}
	
	public abstract List<ItemStack> getFuelStacks(Gas gasType);
}
