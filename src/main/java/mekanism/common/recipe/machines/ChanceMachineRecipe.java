package mekanism.common.recipe.machines;

import mekanism.common.recipe.inputs.ItemStackInput;
import mekanism.common.recipe.outputs.ChanceOutput;

import net.minecraft.item.ItemStack;

public abstract class ChanceMachineRecipe<RECIPE extends ChanceMachineRecipe<RECIPE>> extends MachineRecipe<ItemStackInput, ChanceOutput, RECIPE>
{
	public ChanceMachineRecipe(ItemStackInput input, ChanceOutput output)
	{
		super(input, output);
	}

	public boolean canOperate(ItemStack[] inventory, int inputIndex, int primaryIndex, int secondaryIndex)
	{
		return getInput().useItemStackFromInventory(inventory, inputIndex, false) && getOutput().applyOutputs(inventory, primaryIndex, secondaryIndex, false);
	}

	public void operate(ItemStack[] inventory)
	{
		if(getInput().useItemStackFromInventory(inventory, 0, true))
		{
			getOutput().applyOutputs(inventory, 2, 4, true);
		}
	}
}
