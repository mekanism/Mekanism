package mekanism.common.recipe.machines;

import mekanism.api.gas.Gas;
import mekanism.api.gas.GasRegistry;
import mekanism.common.recipe.inputs.AdvancedMachineInput;
import mekanism.common.recipe.outputs.ItemStackOutput;

import net.minecraft.item.ItemStack;

public class AdvancedMachineRecipe extends MachineRecipe<AdvancedMachineInput, ItemStackOutput>
{
	public AdvancedMachineRecipe(ItemStack input, String gasName, ItemStack output)
	{
		super(new AdvancedMachineInput(input, GasRegistry.getGas(gasName)), new ItemStackOutput(output));
	}

	public AdvancedMachineRecipe(ItemStack input, Gas gas, ItemStack output)
	{
		super(new AdvancedMachineInput(input, gas), new ItemStackOutput(output));
	}
}
