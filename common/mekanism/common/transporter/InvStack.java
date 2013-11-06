package mekanism.common.transporter;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public final class InvStack
{
	public IInventory inventory;
	public ItemStack[] itemStacks;
	public int[] slotIDs;
	
	public InvStack(IInventory inv, ItemStack[] stacks, int[] ids)
	{
		inventory = inv;
		itemStacks = stacks;
		slotIDs = ids;
	}
	
	public ItemStack getStack()
	{
		int size = 0;
		
		for(ItemStack stack : itemStacks)
		{
			size += stack.stackSize;
		}
		
		if(itemStacks[0] != null)
		{
			ItemStack ret = itemStacks[0].copy();
			ret.stackSize = size;
			
			return ret;
		}
		
		return null;
	}
	
	public void use()
	{
		for(int id : slotIDs)
		{
			inventory.setInventorySlotContents(id, null);
		}
	}
	
	public void reset()
	{
		for(int i = 0; i < slotIDs.length; i++)
		{
			inventory.setInventorySlotContents(slotIDs[i], itemStacks[i]);
		}
	}
}
