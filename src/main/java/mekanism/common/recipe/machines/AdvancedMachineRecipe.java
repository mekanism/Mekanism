package mekanism.common.recipe.machines;

import mekanism.api.gas.Gas;
import mekanism.api.gas.GasRegistry;
import mekanism.api.gas.GasTank;
import mekanism.common.recipe.inputs.AdvancedMachineInput;
import mekanism.common.recipe.outputs.ItemStackOutput;

import net.minecraft.item.ItemStack;

public abstract class AdvancedMachineRecipe<RECIPE extends AdvancedMachineRecipe<RECIPE>> extends MachineRecipe<AdvancedMachineInput, ItemStackOutput, RECIPE>
{
	public AdvancedMachineRecipe(AdvancedMachineInput input, ItemStackOutput output)
	{
		super(input, output);
	}

	public AdvancedMachineRecipe(ItemStack input, String gasName, ItemStack output)
	{
		this(new AdvancedMachineInput(input, GasRegistry.getGas(gasName)), new ItemStackOutput(output));
	}

	public AdvancedMachineRecipe(ItemStack input, Gas gas, ItemStack output)
	{
		this(new AdvancedMachineInput(input, gas), new ItemStackOutput(output));
	}

	public boolean canOperate(ItemStack[] inventory, int inputIndex, int outputIndex, GasTank gasTank, int amount)
	{
		return getInput().useItem(inventory, inputIndex, false) && getInput().useSecondary(gasTank, amount, false) && getOutput().applyOutputs(inventory, outputIndex, false);
	}

	public void operate(ItemStack[] inventory, int inputIndex, int outputIndex, GasTank gasTank, int needed)
	{
		if(getInput().useItem(inventory, inputIndex, true) && getInput().useSecondary(gasTank, needed, true))
		{
			getOutput().applyOutputs(inventory, outputIndex, true);
		}

	}
}
