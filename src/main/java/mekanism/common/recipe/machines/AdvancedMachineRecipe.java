package mekanism.common.recipe.machines;

import mekanism.api.gas.Gas;
import mekanism.api.gas.GasTank;
import mekanism.common.recipe.inputs.AdvancedMachineInput;
import mekanism.common.recipe.outputs.ItemStackOutput;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public abstract class AdvancedMachineRecipe<RECIPE extends AdvancedMachineRecipe<RECIPE>> extends MachineRecipe<AdvancedMachineInput, ItemStackOutput, RECIPE>
{
	public AdvancedMachineRecipe(AdvancedMachineInput input, ItemStackOutput output)
	{
		super(input, output);
	}

	public AdvancedMachineRecipe(ItemStack input, Gas gas, ItemStack output)
	{
		this(new AdvancedMachineInput(input, gas), new ItemStackOutput(output));
	}

	public boolean inputMatches(NonNullList<ItemStack> inventory, int inputIndex, GasTank gasTank, int amount){
		return getInput().useItem(inventory, inputIndex, false) && getInput().useSecondary(gasTank, amount, false);
	}

	public boolean canOperate(NonNullList<ItemStack> inventory, int inputIndex, int outputIndex, GasTank gasTank, int amount)
	{
		 return inputMatches(inventory, inputIndex, gasTank, amount) && getOutput().applyOutputs(inventory, outputIndex, false);
	}

	public void operate(NonNullList<ItemStack> inventory, int inputIndex, int outputIndex, GasTank gasTank, int needed)
	{
		if(getInput().useItem(inventory, inputIndex, true) && getInput().useSecondary(gasTank, needed, true))
		{
			getOutput().applyOutputs(inventory, outputIndex, true);
		}
	}
}
