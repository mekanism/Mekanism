package mekanism.common.recipe.outputs;

import net.minecraft.item.ItemStack;

public class ItemStackOutput extends MachineOutput
{
	public ItemStack output;

	public ItemStackOutput(ItemStack stack)
	{
		output = stack;
	}
}
