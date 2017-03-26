package mekanism.client.jei.machine;

import mekanism.common.recipe.inputs.AdvancedMachineInput;
import mekanism.common.recipe.machines.BasicMachineRecipe;
import mekanism.common.recipe.outputs.ItemStackOutput;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.BlankRecipeWrapper;
import net.minecraft.item.ItemStack;

public class MachineRecipeWrapper extends BlankRecipeWrapper
{
	public BasicMachineRecipe recipe;
	
	public MachineRecipeCategory category;
	
	public MachineRecipeWrapper(BasicMachineRecipe r, MachineRecipeCategory c)
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
}
