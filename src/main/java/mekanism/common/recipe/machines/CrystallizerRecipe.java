package mekanism.common.recipe.machines;

import mekanism.api.gas.GasStack;
import mekanism.common.recipe.inputs.GasInput;
import mekanism.common.recipe.outputs.ItemStackOutput;

import net.minecraft.item.ItemStack;

public class CrystallizerRecipe extends MachineRecipe<GasInput, ItemStackOutput>
{
	public CrystallizerRecipe(GasStack input, ItemStack output)
	{
		super(new GasInput(input), new ItemStackOutput(output));
	}
}
