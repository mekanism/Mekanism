package mekanism.common.recipe.machines;

import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import mekanism.common.recipe.inputs.GasInput;
import mekanism.common.recipe.outputs.ItemStackOutput;

import net.minecraft.item.ItemStack;

public class CrystallizerRecipe extends MachineRecipe<GasInput, ItemStackOutput, CrystallizerRecipe>
{
	public CrystallizerRecipe(GasInput input, ItemStackOutput output)
	{
		super(input, output);
	}

	public CrystallizerRecipe(GasStack input, ItemStack output)
	{
		this(new GasInput(input), new ItemStackOutput(output));
	}

	public boolean canOperate(GasTank gasTank, ItemStack[] inventory)
	{
		return getInput().useGas(gasTank, false, 1) && getOutput().applyOutputs(inventory, 1, false);
	}

	public void operate(GasTank inputTank, ItemStack[] inventory)
	{
		if(getInput().useGas(inputTank, true, 1))
		{
			getOutput().applyOutputs(inventory, 1, true);
		}
	}

	@Override
	public CrystallizerRecipe copy()
	{
		return new CrystallizerRecipe(getInput().copy(), getOutput().copy());
	}
}
