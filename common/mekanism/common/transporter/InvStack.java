package mekanism.common.transporter;

import java.util.ArrayList;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public final class InvStack
{
	public IInventory inventory;
	public ArrayList<ItemStack> itemStacks;
	public ArrayList<Integer> slotIDs;
	
	public InvStack(IInventory inv)
	{
		inventory = inv;
		itemStacks = new ArrayList<ItemStack>();
		slotIDs = new ArrayList<Integer>();
	}
	
	public InvStack(IInventory inv, int id, ItemStack stack)
	{
		inventory = inv;
		itemStacks = new ArrayList<ItemStack>();
		slotIDs = new ArrayList<Integer>();
		
		appendStack(id, stack);
	}
	
	public ItemStack getStack()
	{
		int size = 0;
		
		for(ItemStack stack : itemStacks)
		{
			size += stack.stackSize;
		}
		
		if(!itemStacks.isEmpty())
		{
			ItemStack ret = itemStacks.get(0).copy();
			ret.stackSize = size;
			
			return ret;
		}
		
		return null;
	}
	
	public void appendStack(int id, ItemStack stack)
	{
		slotIDs.add(id);
		itemStacks.add(stack);
	}
	
	public void use()
	{
		for(int i = 0; i < slotIDs.size(); i++)
		{
			ItemStack stack = itemStacks.get(i);
			
			if(inventory.getStackInSlot(slotIDs.get(i)).stackSize == stack.stackSize)
			{
				inventory.setInventorySlotContents(slotIDs.get(i), null);
			}
			else {
				ItemStack ret = stack.copy();
				ret.stackSize = inventory.getStackInSlot(slotIDs.get(i)).stackSize - stack.stackSize;
				inventory.setInventorySlotContents(slotIDs.get(i), ret);
			}
		}
	}
	
	public void reset()
	{
		for(int i = 0; i < slotIDs.size(); i++)
		{
			inventory.setInventorySlotContents(slotIDs.get(i), itemStacks.get(i));
		}
	}
}
