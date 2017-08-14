package mekanism.common.content.transporter;

import java.util.ArrayList;

import mekanism.common.util.InventoryUtils;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.items.IItemHandler;

public final class InvStack
{
	public TileEntity tileEntity;
	public ArrayList<ItemStack> itemStacks;
	public ArrayList<Integer> slotIDs;
	public EnumFacing side;

	public InvStack(TileEntity inv, EnumFacing facing)
	{
		tileEntity = inv;
		itemStacks = new ArrayList<ItemStack>();
		slotIDs = new ArrayList<Integer>();
		side = facing;
	}

	public InvStack(TileEntity inv, int id, ItemStack stack, EnumFacing facing)
	{
		tileEntity = inv;
		itemStacks = new ArrayList<ItemStack>();
		slotIDs = new ArrayList<Integer>();
		side = facing;

		if (stack != null)
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
		if (stack == null)
			return;
		slotIDs.add(id);
		itemStacks.add(stack);
	}

	public void use(int amount)
	{
		if(tileEntity instanceof IInventory)
		{
			IInventory inventory = InventoryUtils.checkChestInv((IInventory)tileEntity);
			
			for(int i = 0; i < slotIDs.size(); i++)
			{
				ItemStack stack = itemStacks.get(i);
				
				if(inventory.getStackInSlot(slotIDs.get(i)).stackSize == stack.stackSize && stack.stackSize <= amount)
				{
					inventory.setInventorySlotContents(slotIDs.get(i), null);
					amount -= stack.stackSize;
				}
				else {
					ItemStack ret = stack.copy();
					int toUse = Math.min(amount, stack.stackSize);
					ret.stackSize = inventory.getStackInSlot(slotIDs.get(i)).stackSize - toUse;
					inventory.setInventorySlotContents(slotIDs.get(i), ret);
					amount -= toUse;
				}
				
				if(amount == 0)
				{
					return;
				}
			}
		}
		else if(InventoryUtils.isItemHandler(tileEntity, side))
		{
			IItemHandler handler = InventoryUtils.getItemHandler(tileEntity, side);
			
			for(int i = 0; i < slotIDs.size(); i++)
			{
				ItemStack stack = itemStacks.get(i);
				int toUse = Math.min(amount, stack.stackSize);
				handler.extractItem(slotIDs.get(i), toUse, false);
				amount -= toUse;
				
				if(amount == 0)
				{
					return;
				}
			}
		}
	}

	public void use()
	{
		if (getStack() != null)
			use(getStack().stackSize);
	}
}
