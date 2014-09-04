package mekanism.common.recipe.machines;

import mekanism.api.gas.GasStack;
import mekanism.common.recipe.inputs.ItemStackInput;
import mekanism.common.recipe.outputs.GasOutput;

import net.minecraft.item.ItemStack;

public class OxidationRecipe extends MachineRecipe<ItemStackInput, GasOutput>
{
	public OxidationRecipe(ItemStack input, GasStack output)
	{
		super(new ItemStackInput(input), new GasOutput(output));
	}
}
