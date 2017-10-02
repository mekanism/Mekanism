package mekanism.common.recipe.machines;

import mekanism.common.recipe.inputs.DoubleMachineInput;
import mekanism.common.recipe.outputs.ItemStackOutput;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public abstract class DoubleMachineRecipe<RECIPE extends DoubleMachineRecipe<RECIPE>> extends MachineRecipe<DoubleMachineInput, ItemStackOutput, RECIPE>
{
	public DoubleMachineRecipe(DoubleMachineInput input, ItemStackOutput output)
	{
		super(input, output);
	}

	public DoubleMachineRecipe(ItemStack input, ItemStack extra, ItemStack output)
	{
		this(new DoubleMachineInput(input, extra), new ItemStackOutput(output));
	}

	public boolean canOperate(NonNullList<ItemStack> inventory, int inputIndex, int extraIndex, int outputIndex)
	{
		return getInput().useItem(inventory, inputIndex, false) && getInput().useExtra(inventory, extraIndex, false) && getOutput().applyOutputs(inventory, outputIndex, false);
	}

	public void operate(NonNullList<ItemStack> inventory, int inputIndex, int extraIndex, int outputIndex)
	{
		if(getInput().useItem(inventory, inputIndex, true) && getInput().useExtra(inventory, extraIndex, true))
		{
			getOutput().applyOutputs(inventory, outputIndex, true);
		}
	}
}
