package mekanism.common.recipe.outputs;

import net.minecraft.item.ItemStack;

public class ItemStackOutput extends MachineOutput<ItemStackOutput>
{
	public ItemStack output;

	public ItemStackOutput(ItemStack stack)
	{
		output = stack;
	}

	public boolean applyOutputs(ItemStack[] inventory, int index, boolean doEmit)
	{
		if(inventory[index] == null)
		{
			if(doEmit)
			{
				inventory[index] = output.copy();
			}
			return true;
		} else if(inventory[index].isItemEqual(output) && inventory[index].stackSize + output.stackSize <= inventory[index].getMaxStackSize())
		{
			if(doEmit)
			{
				inventory[index].stackSize += output.stackSize;
			}
			return true;
		}
		return false;
	}

	@Override
	public ItemStackOutput copy()
	{
		return new ItemStackOutput(output.copy());
	}
}
