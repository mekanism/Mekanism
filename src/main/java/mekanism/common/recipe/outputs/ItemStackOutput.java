package mekanism.common.recipe.outputs;

import mekanism.common.util.InventoryUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class ItemStackOutput extends MachineOutput<ItemStackOutput>
{
	public ItemStack output;

	public ItemStackOutput(ItemStack stack)
	{
		output = stack;
	}
	
	public ItemStackOutput() {}
	
	@Override
	public void load(NBTTagCompound nbtTags)
	{
		output = InventoryUtils.loadFromNBT(nbtTags.getCompoundTag("output"));
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
		} 
		else if(inventory[index].isItemEqual(output) && inventory[index].getCount() + output.getCount() <= inventory[index].getMaxStackSize())
		{
			if(doEmit)
			{
				inventory[index].grow(output.getCount());
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
