package mekanism.common.recipe.machines;

import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import mekanism.common.recipe.inputs.ItemStackInput;
import mekanism.common.recipe.outputs.GasOutput;

import net.minecraft.item.ItemStack;

public class OxidationRecipe extends MachineRecipe<ItemStackInput, GasOutput, OxidationRecipe>
{
	public OxidationRecipe(ItemStackInput input, GasOutput output)
	{
		super(input, output);
	}

	public OxidationRecipe(ItemStack input, GasStack output)
	{
		this(new ItemStackInput(input), new GasOutput(output));
	}

	@Override
	public OxidationRecipe copy()
	{
		return new OxidationRecipe(getInput().copy(), getOutput().copy());
	}

	public boolean canOperate(ItemStack[] inventory, GasTank outputTank)
	{
		return getInput().useItemStackFromInventory(inventory, 0, false) && getOutput().applyOutputs(outputTank, false, 1);
	}

	public void operate(ItemStack[] inventory, GasTank outputTank)
	{
		if(getInput().useItemStackFromInventory(inventory, 0, true))
		{
			getOutput().applyOutputs(outputTank, true, 1);
		}
	}
}
