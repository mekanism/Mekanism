package mekanism.client.jei.machine;

import mekanism.common.recipe.inputs.ItemStackInput;
import mekanism.common.recipe.machines.BasicMachineRecipe;
import mekanism.common.recipe.outputs.ItemStackOutput;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.item.ItemStack;

public class MachineRecipeWrapper implements IRecipeWrapper
{
	private final BasicMachineRecipe recipe;
	
	public MachineRecipeWrapper(BasicMachineRecipe r)
	{
		recipe = r;
	}
	
	@Override
	public void getIngredients(IIngredients ingredients) 
	{
		ingredients.setInput(ItemStack.class, ((ItemStackInput)recipe.getInput()).ingredient);
		ingredients.setOutput(ItemStack.class, ((ItemStackOutput)recipe.getOutput()).output);
	}

	public BasicMachineRecipe getRecipe()
	{
		return recipe;
	}
}
