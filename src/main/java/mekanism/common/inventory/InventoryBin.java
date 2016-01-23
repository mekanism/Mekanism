package mekanism.common.inventory;

import mekanism.api.util.StackUtils;
import mekanism.common.item.ItemBlockBasic;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class InventoryBin
{
	public final int MAX_STORAGE = 4096;

	public ItemStack bin;

	public InventoryBin(ItemStack stack)
	{
		bin = stack;
	}

	public ItemStack getStack()
	{
		if(getItemCount() > 0 && getItemType() != null)
		{
			ItemStack ret = getItemType().copy();
			ret.stackSize = Math.min(getItemType().getMaxStackSize(), getItemCount());

			return ret;
		}

		return null;
	}

	public ItemStack removeStack()
	{
		ItemStack stack = getStack();

		if(stack == null)
		{
			return null;
		}

		setItemCount(getItemCount() - stack.stackSize);
		return stack.copy();
	}

	public ItemStack add(ItemStack stack)
	{
		if(isValid(stack) && getItemCount() != MAX_STORAGE)
		{
			if(getItemType() == null)
			{
				setItemType(stack);
			}

			if(getItemCount() + stack.stackSize <= MAX_STORAGE)
			{
				setItemCount(getItemCount() + stack.stackSize);
				return null;
			}
			else {
				ItemStack rejects = getItemType().copy();
				rejects.stackSize = (getItemCount()+stack.stackSize) - MAX_STORAGE;

				setItemCount(MAX_STORAGE);

				return rejects;
			}
		}

		return stack;
	}

	public boolean isValid(ItemStack stack)
	{
		if(stack == null || stack.stackSize <= 0)
		{
			return false;
		}

		if(stack.getItem() instanceof ItemBlockBasic && stack.getItemDamage() == 6)
		{
			return false;
		}

		if(getItemType() == null)
		{
			return true;
		}

		if(!stack.isItemEqual(getItemType()) || !ItemStack.areItemStackTagsEqual(stack, getItemType()))
		{
			return false;
		}

		return true;
	}

	public int getItemCount()
	{
		if(bin.getTagCompound() == null)
		{
			return 0;
		}

		return bin.getTagCompound().getInteger("itemCount");
	}

	public void setItemCount(int count)
	{
		if(bin.getTagCompound() == null)
		{
			bin.setTagCompound(new NBTTagCompound());
		}

		bin.getTagCompound().setInteger("itemCount", Math.max(0, count));

		if(getItemCount() == 0)
		{
			setItemType(null);
		}
	}

	public ItemStack getItemType()
	{
		if(bin.getTagCompound() == null || getItemCount() == 0)
		{
			return null;
		}

		return ItemStack.loadItemStackFromNBT(bin.getTagCompound().getCompoundTag("storedItem"));
	}

	public void setItemType(ItemStack stack)
	{
		if(bin.getTagCompound() == null)
		{
			bin.setTagCompound(new NBTTagCompound());
		}

		if(stack == null)
		{
			bin.getTagCompound().removeTag("storedItem");
			return;
		}

		ItemStack ret = StackUtils.size(stack, 1);

		bin.getTagCompound().setTag("storedItem", StackUtils.size(stack, 1).writeToNBT(new NBTTagCompound()));
	}
}
