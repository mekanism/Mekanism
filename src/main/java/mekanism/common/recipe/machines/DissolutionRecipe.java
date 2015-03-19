package mekanism.common.recipe.machines;

import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import mekanism.common.recipe.inputs.ItemStackInput;
import mekanism.common.recipe.outputs.GasOutput;

import net.minecraft.item.ItemStack;

public class DissolutionRecipe extends MachineRecipe<ItemStackInput, GasOutput, DissolutionRecipe>
{
	public DissolutionRecipe(ItemStackInput input, GasOutput output)
	{
		super(input, output);
	}

	public DissolutionRecipe(ItemStack input, GasStack output)
	{
		this(new ItemStackInput(input), new GasOutput(output));
	}

	public boolean canOperate(ItemStack[] inventory, GasTank outputTank)
	{
		return getInput().useItemStackFromInventory(inventory, 1, false) && getOutput().applyOutputs(outputTank, false, 1);
	}

	public void operate(ItemStack[] inventory, GasTank outputTank)
	{
		if(getInput().useItemStackFromInventory(inventory, 1, true))
		{
			getOutput().applyOutputs(outputTank, true, 1);
		}
	}

	@Override
	public DissolutionRecipe copy()
	{
		return new DissolutionRecipe(getInput().copy(), getOutput().copy());
	}
}
