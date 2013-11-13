package mekanism.common.inventory;

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
		if(getItemCount() > 0)
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
		
		if(stack.isItemStackDamageable() && stack.isItemDamaged())
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
		if(bin.stackTagCompound == null)
		{
			return 0;
		}
		
		return bin.stackTagCompound.getInteger("itemCount");
	}
	
	public void setItemCount(int count)
	{
		if(bin.stackTagCompound == null)
		{
			bin.setTagCompound(new NBTTagCompound());
		}
		
		bin.stackTagCompound.setInteger("itemCount", Math.max(0, count));
		
		if(getItemCount() == 0)
		{
			setItemType(null);
		}
	}
	
	public ItemStack getItemType()
	{
		if(bin.stackTagCompound == null || getItemCount() == 0)
		{
			return null;
		}
		
		int id = bin.stackTagCompound.getInteger("itemID");
		int meta = bin.stackTagCompound.getInteger("itemMeta");
		
		if(getItemCount() == 0 || id == 0)
		{
			setItemType(null);
			return null;
		}
		
		return new ItemStack(id, 1, meta);
	}
	
	public void setItemType(ItemStack stack)
	{
		if(bin.stackTagCompound == null)
		{
			bin.setTagCompound(new NBTTagCompound());
		}
		
		if(stack == null)
		{
			bin.stackTagCompound.removeTag("itemID");
			bin.stackTagCompound.removeTag("itemMeta");
			return;
		}
		
		ItemStack ret = stack.copy();
		ret.stackSize = 1;
		
		bin.stackTagCompound.setInteger("itemID", stack.itemID);
		bin.stackTagCompound.setInteger("itemMeta", stack.getItemDamage());
	}
}
