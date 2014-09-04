package mekanism.common.recipe.machines;

import mekanism.common.recipe.inputs.ItemStackInput;
import mekanism.common.recipe.outputs.ItemStackOutput;

import net.minecraft.item.ItemStack;

public class BasicMachineRecipe extends MachineRecipe<ItemStackInput, ItemStackOutput>
{
	public BasicMachineRecipe(ItemStack input, ItemStack output)
	{
		super(new ItemStackInput(input), new ItemStackOutput(output));
	}
}
