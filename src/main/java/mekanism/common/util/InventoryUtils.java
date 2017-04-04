package mekanism.common.util;

import mekanism.api.EnumColor;
import mekanism.api.util.StackUtils;
import mekanism.common.base.ISideConfiguration;
import mekanism.common.content.transporter.Finder;
import mekanism.common.content.transporter.InvStack;
import mekanism.common.tile.TileEntityBin;
import mekanism.common.tile.TileEntityLogisticalSorter;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.InventoryLargeChest;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.EnumFacing;

public final class InventoryUtils
{
	public static final int[] EMPTY = new int[] {};

	public static int[] getIntRange(int start, int end)
	{
		int[] ret = new int[1 + end - start];

		for(int i = start; i <= end; i++)
		{
			ret[i - start] = i;
		}

		return ret;
	}

	public static IInventory checkChestInv(IInventory inv)
	{
		if(inv instanceof TileEntityChest)
		{
			TileEntityChest main = (TileEntityChest)inv;
			TileEntityChest adj = null;

			if(main.adjacentChestXNeg != null)
			{
				adj = main.adjacentChestXNeg;
			}
			else if(main.adjacentChestXPos != null)
			{
				adj = main.adjacentChestXPos;
			}
			else if(main.adjacentChestZNeg != null)
			{
				adj = main.adjacentChestZNeg;
			}
			else if(main.adjacentChestZPos != null)
			{
				adj = main.adjacentChestZPos;
			}

			if(adj != null)
			{
				return new InventoryLargeChest("", main, adj);
			}
		}

		return inv;
	}

	public static ItemStack putStackInInventory(IInventory inventory, ItemStack itemStack, EnumFacing side, boolean force)
	{
		inventory = checkChestInv(inventory);

		if(force && inventory instanceof TileEntityLogisticalSorter)
		{
			return ((TileEntityLogisticalSorter)inventory).sendHome(itemStack.copy());
		}

		ItemStack toInsert = itemStack.copy();

		if(!(inventory instanceof ISidedInventory))
		{
			for(int i = 0; i <= inventory.getSizeInventory() - 1; i++)
			{
				if(!force)
				{
					if(!inventory.isItemValidForSlot(i, toInsert))
					{
						continue;
					}
				}

				ItemStack inSlot = inventory.getStackInSlot(i);

				if(inSlot.isEmpty())
				{
					if(toInsert.getCount() <= inventory.getInventoryStackLimit())
					{
						inventory.setInventorySlotContents(i, toInsert);
						inventory.markDirty();
						
						return ItemStack.EMPTY;
					}
					else {
						int rejects = toInsert.getCount() - inventory.getInventoryStackLimit();
						
						ItemStack toSet = toInsert.copy();
						toSet.setCount(inventory.getInventoryStackLimit());

						ItemStack remains = toInsert.copy();
						remains.setCount(rejects);

						inventory.setInventorySlotContents(i, toSet);
						inventory.markDirty();

						toInsert = remains;
					}
				}
				else if(areItemsStackable(toInsert, inSlot) && inSlot.getCount() < Math.min(inSlot.getMaxStackSize(), inventory.getInventoryStackLimit()))
				{
					int max = Math.min(inSlot.getMaxStackSize(), inventory.getInventoryStackLimit());
					
					if(inSlot.getCount() + toInsert.getCount() <= max)
					{
						ItemStack toSet = toInsert.copy();
						toSet.grow(inSlot.getCount());

						inventory.setInventorySlotContents(i, toSet);
						inventory.markDirty();
						
						return ItemStack.EMPTY;
					}
					else {
						int rejects = (inSlot.getCount() + toInsert.getCount()) - max;

						ItemStack toSet = toInsert.copy();
						toSet.setCount(max);

						ItemStack remains = toInsert.copy();
						remains.setCount(rejects);

						inventory.setInventorySlotContents(i, toSet);
						inventory.markDirty();

						toInsert = remains;
					}
				}
			}
		}
		else {
			ISidedInventory sidedInventory = (ISidedInventory)inventory;
			int[] slots = sidedInventory.getSlotsForFace(side.getOpposite());

			if(slots != null && slots.length != 0)
			{
				if(force && sidedInventory instanceof TileEntityBin && side == EnumFacing.UP)
				{
					slots = sidedInventory.getSlotsForFace(EnumFacing.UP);
				}

				for(int get = 0; get <= slots.length - 1; get++)
				{
					int slotID = slots[get];

					if(!force)
					{
						if(!sidedInventory.isItemValidForSlot(slotID, toInsert) || !sidedInventory.canInsertItem(slotID, toInsert, side.getOpposite()))
						{
							continue;
						}
					}

					ItemStack inSlot = inventory.getStackInSlot(slotID);

					if(inSlot.isEmpty())
					{
						if(toInsert.getCount() <= inventory.getInventoryStackLimit())
						{
							inventory.setInventorySlotContents(slotID, toInsert);
							inventory.markDirty();
							
							return ItemStack.EMPTY;
						}
						else {
							int rejects = toInsert.getCount() - inventory.getInventoryStackLimit();
							
							ItemStack toSet = toInsert.copy();
							toSet.setCount(inventory.getInventoryStackLimit());

							ItemStack remains = toInsert.copy();
							remains.setCount(rejects);

							inventory.setInventorySlotContents(slotID, toSet);
							inventory.markDirty();

							toInsert = remains;
						}
					}
					else if(areItemsStackable(toInsert, inSlot) && inSlot.getCount() < Math.min(inSlot.getMaxStackSize(), inventory.getInventoryStackLimit()))
					{
						int max = Math.min(inSlot.getMaxStackSize(), inventory.getInventoryStackLimit());
						
						if(inSlot.getCount() + toInsert.getCount() <= max)
						{
							ItemStack toSet = toInsert.copy();
							toSet.grow(inSlot.getCount());

							inventory.setInventorySlotContents(slotID, toSet);
							inventory.markDirty();
							
							return ItemStack.EMPTY;
						}
						else {
							int rejects = (inSlot.getCount() + toInsert.getCount()) - max;

							ItemStack toSet = toInsert.copy();
							toSet.setCount(max);

							ItemStack remains = toInsert.copy();
							remains.setCount(rejects);

							inventory.setInventorySlotContents(slotID, toSet);
							inventory.markDirty();

							toInsert = remains;
						}
					}
				}
			}
		}

		return toInsert;
	}

	public static boolean areItemsStackable(ItemStack toInsert, ItemStack inSlot) 
	{
		if(toInsert.isEmpty() || inSlot.isEmpty())
		{
			return true;
		}
		
    	return inSlot.isItemEqual(toInsert) && ItemStack.areItemStackTagsEqual(inSlot, toInsert);
  	}

  	public static InvStack takeTopItem(IInventory inventory, EnumFacing side, int amount)
	{
		inventory = checkChestInv(inventory);

		if(!(inventory instanceof ISidedInventory))
		{
			for(int i = inventory.getSizeInventory() - 1; i >= 0; i--)
			{
				if(!inventory.getStackInSlot(i).isEmpty() && inventory.getStackInSlot(i).getCount() > 0)
				{
					ItemStack toSend = inventory.getStackInSlot(i).copy();
					toSend.setCount(Math.min(amount, toSend.getCount()));

					return new InvStack(inventory, i, toSend);
				}
			}
		}
		else {
			ISidedInventory sidedInventory = (ISidedInventory)inventory;
			int[] slots = sidedInventory.getSlotsForFace(side.getOpposite());

			if(slots != null)
			{
				for(int get = slots.length - 1; get >= 0; get--)
				{
					int slotID = slots[get];

					if(!sidedInventory.getStackInSlot(slotID).isEmpty() && sidedInventory.getStackInSlot(slotID).getCount() > 0)
					{
						ItemStack toSend = sidedInventory.getStackInSlot(slotID).copy();
						toSend.setCount(Math.min(amount, toSend.getCount()));

						if(sidedInventory.canExtractItem(slotID, toSend, side.getOpposite()))
						{
							return new InvStack(inventory, slotID, toSend);
						}
					}
				}
			}
		}

		return null;
	}

	public static InvStack takeDefinedItem(IInventory inventory, EnumFacing side, ItemStack type, int min, int max)
	{
		inventory = checkChestInv(inventory);

		InvStack ret = new InvStack(inventory);

		if(!(inventory instanceof ISidedInventory))
		{
			for(int i = inventory.getSizeInventory() - 1; i >= 0; i--)
			{
				if(!inventory.getStackInSlot(i).isEmpty() && StackUtils.equalsWildcard(inventory.getStackInSlot(i), type))
				{
					ItemStack stack = inventory.getStackInSlot(i);
					int current = !ret.getStack().isEmpty() ? ret.getStack().getCount() : 0;

					if(current+stack.getCount() <= max)
					{
						ret.appendStack(i, stack.copy());
					}
					else {
						ItemStack copy = stack.copy();
						copy.setCount(max-current);
						ret.appendStack(i, copy);
					}

					if(!ret.getStack().isEmpty() && ret.getStack().getCount() == max)
					{
						return ret;
					}
				}
			}
		}
		else {
			ISidedInventory sidedInventory = (ISidedInventory)inventory;
			int[] slots = sidedInventory.getSlotsForFace(side.getOpposite());

			if(slots != null && slots.length != 0)
			{
				for(int get = slots.length - 1; get >= 0; get--)
				{
					int slotID = slots[get];

					if(!sidedInventory.getStackInSlot(slotID).isEmpty() && StackUtils.equalsWildcard(inventory.getStackInSlot(slotID), type))
					{
						ItemStack stack = sidedInventory.getStackInSlot(slotID);
						int current = !ret.getStack().isEmpty() ? ret.getStack().getCount() : 0;

						if(current+stack.getCount() <= max)
						{
							ItemStack copy = stack.copy();

							if(sidedInventory.canExtractItem(slotID, copy, side.getOpposite()))
							{
								ret.appendStack(slotID, copy);
							}
						}
						else {
							ItemStack copy = stack.copy();

							if(sidedInventory.canExtractItem(slotID, copy, side.getOpposite()))
							{
								copy.setCount(max-current);
								ret.appendStack(slotID, copy);
							}
						}

						if(!ret.getStack().isEmpty() && ret.getStack().getCount() == max)
						{
							return ret;
						}
					}
				}
			}
		}

		if(ret != null && !ret.getStack().isEmpty() && ret.getStack().getCount() >= min)
		{
			return ret;
		}

		return null;
	}

	public static InvStack takeTopStack(IInventory inventory, EnumFacing side, Finder id)
	{
		inventory = checkChestInv(inventory);

		if(!(inventory instanceof ISidedInventory))
		{
			for(int i = inventory.getSizeInventory() - 1; i >= 0; i--)
			{
				if(!inventory.getStackInSlot(i).isEmpty() && id.modifies(inventory.getStackInSlot(i)))
				{
					ItemStack toSend = inventory.getStackInSlot(i).copy();
					return new InvStack(inventory, i, toSend);
				}
			}
		}
		else {
			ISidedInventory sidedInventory = (ISidedInventory)inventory;
			int[] slots = sidedInventory.getSlotsForFace(side.getOpposite());

			if(slots != null && slots.length != 0)
			{
				for(int get = slots.length - 1; get >= 0; get--)
				{
					int slotID = slots[get];

					if(!sidedInventory.getStackInSlot(slotID).isEmpty() && id.modifies(sidedInventory.getStackInSlot(slotID)))
					{
						ItemStack toSend = sidedInventory.getStackInSlot(slotID);

						if(sidedInventory.canExtractItem(slotID, toSend, side.getOpposite()))
						{
							return new InvStack(inventory, slotID, toSend);
						}
					}
				}
			}
		}

		return null;
	}

	public static boolean canInsert(TileEntity tileEntity, EnumColor color, ItemStack itemStack, EnumFacing side, boolean force)
	{
		if(!(tileEntity instanceof IInventory))
		{
			return false;
		}

		if(force && tileEntity instanceof TileEntityLogisticalSorter)
		{
			return ((TileEntityLogisticalSorter)tileEntity).canSendHome(itemStack);
		}

		if(!force && tileEntity instanceof ISideConfiguration)
		{
			ISideConfiguration config = (ISideConfiguration)tileEntity;
			EnumFacing tileSide = config.getOrientation();
			EnumColor configColor = config.getEjector().getInputColor(MekanismUtils.getBaseOrientation(side, tileSide).getOpposite());

			if(config.getEjector().hasStrictInput() && configColor != null && configColor != color)
			{
				return false;
			}
		}

		IInventory inventory = checkChestInv((IInventory)tileEntity);

		if(!(inventory instanceof ISidedInventory))
		{
			for(int i = 0; i <= inventory.getSizeInventory() - 1; i++)
			{
				if(!force)
				{
					if(!inventory.isItemValidForSlot(i, itemStack))
					{
						continue;
					}
				}

				ItemStack inSlot = inventory.getStackInSlot(i);

				if(inSlot.isEmpty())
				{
					if(itemStack.getCount() <= inventory.getInventoryStackLimit())
					{
						return true;
					}
					else {
						int rejects = itemStack.getCount() - inventory.getInventoryStackLimit();
						
						if(rejects < itemStack.getCount())
						{
							return true;
						}
					}
				}
				else if(areItemsStackable(itemStack, inSlot) && inSlot.getCount() < Math.min(inSlot.getMaxStackSize(), inventory.getInventoryStackLimit()))
				{
					int max = Math.min(inSlot.getMaxStackSize(), inventory.getInventoryStackLimit());
					
					if(inSlot.getCount() + itemStack.getCount() <= max)
					{
						return true;
					}
					else {
						int rejects = (inSlot.getCount() + itemStack.getCount()) - max;

						if(rejects < itemStack.getCount())
						{
							return true;
						}
					}
				}
			}
		}
		else {
			ISidedInventory sidedInventory = (ISidedInventory)inventory;
			int[] slots = sidedInventory.getSlotsForFace(side.getOpposite());

			if(slots != null && slots.length != 0)
			{
				if(force && sidedInventory instanceof TileEntityBin && side == EnumFacing.UP)
				{
					slots = sidedInventory.getSlotsForFace(EnumFacing.UP);
				}

				for(int get = 0; get <= slots.length - 1; get++)
				{
					int slotID = slots[get];

					if(!force)
					{
						if(!sidedInventory.isItemValidForSlot(slotID, itemStack) || !sidedInventory.canInsertItem(slotID, itemStack, side.getOpposite()))
						{
							continue;
						}
					}

					ItemStack inSlot = inventory.getStackInSlot(slotID);

					if(inSlot.isEmpty())
					{
						if(itemStack.getCount() <= inventory.getInventoryStackLimit())
						{
							return true;
						}
						else {
							int rejects = itemStack.getCount() - inventory.getInventoryStackLimit();
							
							if(rejects < itemStack.getCount())
							{
								return true;
							}
						}
					}
					else if(areItemsStackable(itemStack, inSlot) && inSlot.getCount() < Math.min(inSlot.getMaxStackSize(), inventory.getInventoryStackLimit()))
					{
						int max = Math.min(inSlot.getMaxStackSize(), inventory.getInventoryStackLimit());
						
						if(inSlot.getCount() + itemStack.getCount() <= max)
						{
							return true;
						}
						else {
							int rejects = (inSlot.getCount() + itemStack.getCount()) - max;

							if(rejects < itemStack.getCount())
							{
								return true;
							}
						}
					}
				}
			}
		}

		return false;
	}
	
	public static ItemStack loadFromNBT(NBTTagCompound nbtTags)
	{
		ItemStack ret = new ItemStack(nbtTags);
		return ret;
	}

	/*TODO From CCLib -- go back to that version when we're using dependencies again*/
	public static boolean canStack(ItemStack stack1, ItemStack stack2) {
		return stack1.isEmpty() || stack2.isEmpty() ||
				(stack1.getItem() == stack2.getItem() &&
						(!stack2.getHasSubtypes() || stack2.getItemDamage() == stack1.getItemDamage()) &&
						ItemStack.areItemStackTagsEqual(stack2, stack1)) &&
						stack1.isStackable();
	}
}
