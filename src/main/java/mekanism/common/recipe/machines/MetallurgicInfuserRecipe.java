package mekanism.common.recipe.machines;

import mekanism.common.recipe.inputs.InfusionInput;
import mekanism.common.recipe.outputs.InfusionOutput;

import net.minecraft.item.ItemStack;

public class MetallurgicInfuserRecipe extends MachineRecipe<InfusionInput, InfusionOutput>
{
	public MetallurgicInfuserRecipe(InfusionInput input, ItemStack output)
	{
		super(input, InfusionOutput.getInfusion(input, output));
	}
}
