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
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraftforge.common.util.ForgeDirection;

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

	public static ItemStack putStackInInventory(IInventory inventory, ItemStack itemStack, int side, boolean force)
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

				if(inSlot == null)
				{
					inventory.setInventorySlotContents(i, toInsert);
					inventory.markDirty();
					
					return null;
				}
				else if(areItemsStackable(toInsert, inSlot) && inSlot.stackSize < inSlot.getMaxStackSize() && inSlot.stackSize < inventory.getInventoryStackLimit())
				{
					if(inSlot.stackSize + toInsert.stackSize <= inSlot.getMaxStackSize())
					{
						ItemStack toSet = toInsert.copy();
						toSet.stackSize += inSlot.stackSize;

						inventory.setInventorySlotContents(i, toSet);
						inventory.markDirty();
						
						return null;
					}
					else {
						int rejects = (inSlot.stackSize + toInsert.stackSize) - inSlot.getMaxStackSize();

						ItemStack toSet = toInsert.copy();
						toSet.stackSize = inSlot.getMaxStackSize();

						ItemStack remains = toInsert.copy();
						remains.stackSize = rejects;

						inventory.setInventorySlotContents(i, toSet);
						inventory.markDirty();

						toInsert = remains;
					}
				}
			}
		}
		else {
			ISidedInventory sidedInventory = (ISidedInventory)inventory;
			int[] slots = sidedInventory.getAccessibleSlotsFromSide(ForgeDirection.OPPOSITES[side]);

			if(slots != null && slots.length != 0)
			{
				if(force && sidedInventory instanceof TileEntityBin && ForgeDirection.OPPOSITES[side] == 0)
				{
					slots = sidedInventory.getAccessibleSlotsFromSide(1);
				}

				for(int get = 0; get <= slots.length - 1; get++)
				{
					int slotID = slots[get];

					if(!force)
					{
						if(!sidedInventory.isItemValidForSlot(slotID, toInsert) && !sidedInventory.canInsertItem(slotID, toInsert, ForgeDirection.OPPOSITES[side]))
						{
							continue;
						}
					}

					ItemStack inSlot = inventory.getStackInSlot(slotID);

					if(inSlot == null)
					{
						inventory.setInventorySlotContents(slotID, toInsert);
						inventory.markDirty();
						
						return null;
					}
					else if(areItemsStackable(toInsert, inSlot) && inSlot.stackSize < inSlot.getMaxStackSize() && inSlot.stackSize < inventory.getInventoryStackLimit())
					{
						if(inSlot.stackSize + toInsert.stackSize <= inSlot.getMaxStackSize())
						{
							ItemStack toSet = toInsert.copy();
							toSet.stackSize += inSlot.stackSize;

							inventory.setInventorySlotContents(slotID, toSet);
							inventory.markDirty();
							
							return null;
						}
						else {
							int rejects = (inSlot.stackSize + toInsert.stackSize) - inSlot.getMaxStackSize();

							ItemStack toSet = toInsert.copy();
							toSet.stackSize = inSlot.getMaxStackSize();

							ItemStack remains = toInsert.copy();
							remains.stackSize = rejects;

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
    	return inSlot.isItemEqual(toInsert) && ItemStack.areItemStackTagsEqual(inSlot, toInsert);
  	}

  	public static InvStack takeTopItem(IInventory inventory, int side, int amount)
	{
		inventory = checkChestInv(inventory);

		if(!(inventory instanceof ISidedInventory))
		{
			for(int i = inventory.getSizeInventory() - 1; i >= 0; i--)
			{
				if(inventory.getStackInSlot(i) != null && inventory.getStackInSlot(i).stackSize > 0)
				{
					ItemStack toSend = inventory.getStackInSlot(i).copy();
					toSend.stackSize = Math.min(amount, toSend.stackSize);

					return new InvStack(inventory, i, toSend);
				}
			}
		}
		else {
			ISidedInventory sidedInventory = (ISidedInventory)inventory;
			int[] slots = sidedInventory.getAccessibleSlotsFromSide(ForgeDirection.OPPOSITES[side]);

			if(slots != null)
			{
				for(int get = slots.length - 1; get >= 0; get--)
				{
					int slotID = slots[get];

					if(sidedInventory.getStackInSlot(slotID) != null && sidedInventory.getStackInSlot(slotID).stackSize > 0)
					{
						ItemStack toSend = sidedInventory.getStackInSlot(slotID).copy();
						toSend.stackSize = Math.min(amount, toSend.stackSize);

						if(sidedInventory.canExtractItem(slotID, toSend, ForgeDirection.OPPOSITES[side]))
						{
							return new InvStack(inventory, slotID, toSend);
						}
					}
				}
			}
		}

		return null;
	}

	public static InvStack takeDefinedItem(IInventory inventory, int side, ItemStack type, int min, int max)
	{
		inventory = checkChestInv(inventory);

		InvStack ret = new InvStack(inventory);

		if(!(inventory instanceof ISidedInventory))
		{
			for(int i = inventory.getSizeInventory() - 1; i >= 0; i--)
			{
				if(inventory.getStackInSlot(i) != null && StackUtils.equalsWildcard(inventory.getStackInSlot(i), type))
				{
					ItemStack stack = inventory.getStackInSlot(i);
					int current = ret.getStack() != null ? ret.getStack().stackSize : 0;

					if(current+stack.stackSize <= max)
					{
						ret.appendStack(i, stack.copy());
					}
					else {
						ItemStack copy = stack.copy();
						copy.stackSize = max-current;
						ret.appendStack(i, copy);
					}

					if(ret.getStack() != null && ret.getStack().stackSize == max)
					{
						return ret;
					}
				}
			}
		}
		else {
			ISidedInventory sidedInventory = (ISidedInventory)inventory;
			int[] slots = sidedInventory.getAccessibleSlotsFromSide(ForgeDirection.OPPOSITES[side]);

			if(slots != null && slots.length != 0)
			{
				for(int get = slots.length - 1; get >= 0; get--)
				{
					int slotID = slots[get];

					if(sidedInventory.getStackInSlot(slotID) != null && StackUtils.equalsWildcard(inventory.getStackInSlot(slotID), type))
					{
						ItemStack stack = sidedInventory.getStackInSlot(slotID);
						int current = ret.getStack() != null ? ret.getStack().stackSize : 0;

						if(current+stack.stackSize <= max)
						{
							ItemStack copy = stack.copy();

							if(sidedInventory.canExtractItem(slotID, copy, ForgeDirection.OPPOSITES[side]))
							{
								ret.appendStack(slotID, copy);
							}
						}
						else {
							ItemStack copy = stack.copy();

							if(sidedInventory.canExtractItem(slotID, copy, ForgeDirection.OPPOSITES[side]))
							{
								copy.stackSize = max-current;
								ret.appendStack(slotID, copy);
							}
						}

						if(ret.getStack() != null && ret.getStack().stackSize == max)
						{
							return ret;
						}
					}
				}
			}
		}

		if(ret != null && ret.getStack() != null && ret.getStack().stackSize >= min)
		{
			return ret;
		}

		return null;
	}

	public static InvStack takeTopStack(IInventory inventory, int side, Finder id)
	{
		inventory = checkChestInv(inventory);

		if(!(inventory instanceof ISidedInventory))
		{
			for(int i = inventory.getSizeInventory() - 1; i >= 0; i--)
			{
				if(inventory.getStackInSlot(i) != null && id.modifies(inventory.getStackInSlot(i)))
				{
					ItemStack toSend = inventory.getStackInSlot(i).copy();
					return new InvStack(inventory, i, toSend);
				}
			}
		}
		else {
			ISidedInventory sidedInventory = (ISidedInventory)inventory;
			int[] slots = sidedInventory.getAccessibleSlotsFromSide(ForgeDirection.OPPOSITES[side]);

			if(slots != null && slots.length != 0)
			{
				for(int get = slots.length - 1; get >= 0; get--)
				{
					int slotID = slots[get];

					if(sidedInventory.getStackInSlot(slotID) != null && id.modifies(sidedInventory.getStackInSlot(slotID)))
					{
						ItemStack toSend = sidedInventory.getStackInSlot(slotID);

						if(sidedInventory.canExtractItem(slotID, toSend, ForgeDirection.OPPOSITES[side]))
						{
							return new InvStack(inventory, slotID, toSend);
						}
					}
				}
			}
		}

		return null;
	}

	public static boolean canInsert(TileEntity tileEntity, EnumColor color, ItemStack itemStack, int side, boolean force)
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
			int tileSide = config.getOrientation();
			EnumColor configColor = config.getEjector().getInputColor(ForgeDirection.getOrientation(MekanismUtils.getBaseOrientation(side, tileSide)).getOpposite());

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

				if(inSlot == null)
				{
					return true;
				}
				else if(areItemsStackable(itemStack, inSlot) && inSlot.stackSize < inSlot.getMaxStackSize() && inSlot.stackSize < inventory.getInventoryStackLimit())
				{
					if(inSlot.stackSize + itemStack.stackSize <= inSlot.getMaxStackSize())
					{
						return true;
					}
					else {
						int rejects = (inSlot.stackSize + itemStack.stackSize) - inSlot.getMaxStackSize();

						if(rejects < itemStack.stackSize)
						{
							return true;
						}
					}
				}
			}
		}
		else {
			ISidedInventory sidedInventory = (ISidedInventory)inventory;
			int[] slots = sidedInventory.getAccessibleSlotsFromSide(ForgeDirection.OPPOSITES[side]);

			if(slots != null && slots.length != 0)
			{
				if(force && sidedInventory instanceof TileEntityBin && ForgeDirection.OPPOSITES[side] == 0)
				{
					slots = sidedInventory.getAccessibleSlotsFromSide(1);
				}

				for(int get = 0; get <= slots.length - 1; get++)
				{
					int slotID = slots[get];

					if(!force)
					{
						if(!sidedInventory.isItemValidForSlot(slotID, itemStack) || !sidedInventory.canInsertItem(slotID, itemStack, ForgeDirection.OPPOSITES[side]))
						{
							continue;
						}
					}

					ItemStack inSlot = inventory.getStackInSlot(slotID);

					if(inSlot == null)
					{
						return true;
					}
					else if(areItemsStackable(itemStack, inSlot) && inSlot.stackSize < inSlot.getMaxStackSize() && inSlot.stackSize < inventory.getInventoryStackLimit())
					{
						if(inSlot.stackSize + itemStack.stackSize <= inSlot.getMaxStackSize())
						{
							return true;
						}
						else {
							int rejects = (inSlot.stackSize + itemStack.stackSize) - inSlot.getMaxStackSize();

							if(rejects < itemStack.stackSize)
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
}
