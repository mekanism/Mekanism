package mekanism.common.recipe.machines;

import mekanism.common.InfuseStorage;
import mekanism.common.recipe.inputs.InfusionInput;
import mekanism.common.recipe.outputs.ItemStackOutput;

import net.minecraft.item.ItemStack;

public class MetallurgicInfuserRecipe extends MachineRecipe<InfusionInput, ItemStackOutput, MetallurgicInfuserRecipe>
{
	public MetallurgicInfuserRecipe(InfusionInput input, ItemStackOutput output)
	{
		super(input, output);
	}

	public MetallurgicInfuserRecipe(InfusionInput input, ItemStack output)
	{
		this(input, new ItemStackOutput(output));
	}

	public boolean canOperate(ItemStack[] inventory, InfuseStorage infuse)
	{
		return getInput().use(inventory, 2, infuse, false) && getOutput().applyOutputs(inventory, 3, false);
	}

	@Override
	public MetallurgicInfuserRecipe copy()
	{
		return new MetallurgicInfuserRecipe(getInput(), getOutput());
	}

	public void output(ItemStack[] inventory, InfuseStorage infuseStored)
	{
		if(getInput().use(inventory, 2, infuseStored, true))
		{
			getOutput().applyOutputs(inventory, 3, true);
		}
	}
}
