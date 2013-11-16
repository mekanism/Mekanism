package mekanism.induction.common.base;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;

public final class InventoryUtil
{
	public static ItemStack putStackInInventory(IInventory inventory, ItemStack itemStack, int side)
	{
		if (!(inventory instanceof ISidedInventory))
		{
			for (int i = 0; i <= inventory.getSizeInventory() - 1; i++)
			{
				if (inventory.isItemValidForSlot(i, itemStack))
				{
					ItemStack inSlot = inventory.getStackInSlot(i);

					if (inSlot == null)
					{
						inventory.setInventorySlotContents(i, itemStack);
						return null;
					}
					else if (inSlot.isItemEqual(itemStack) && inSlot.stackSize < inSlot.getMaxStackSize())
					{
						if (inSlot.stackSize + itemStack.stackSize <= inSlot.getMaxStackSize())
						{
							ItemStack toSet = itemStack.copy();
							toSet.stackSize += inSlot.stackSize;

							inventory.setInventorySlotContents(i, toSet);
							return null;
						}
						else
						{
							int rejects = (inSlot.stackSize + itemStack.stackSize) - inSlot.getMaxStackSize();

							ItemStack toSet = itemStack.copy();
							toSet.stackSize = inSlot.getMaxStackSize();

							ItemStack remains = itemStack.copy();
							remains.stackSize = rejects;

							inventory.setInventorySlotContents(i, toSet);
							return remains;
						}
					}
				}
			}
		}
		else
		{
			ISidedInventory sidedInventory = (ISidedInventory) inventory;
			int[] slots = sidedInventory.getAccessibleSlotsFromSide(side);

			for (int get = 0; get <= slots.length - 1; get++)
			{
				int slotID = slots[get];

				if (sidedInventory.isItemValidForSlot(slotID, itemStack) && sidedInventory.canInsertItem(slotID, itemStack, side))
				{
					ItemStack inSlot = inventory.getStackInSlot(slotID);

					if (inSlot == null)
					{
						inventory.setInventorySlotContents(slotID, itemStack);
						return null;
					}
					else if (inSlot.isItemEqual(itemStack) && inSlot.stackSize < inSlot.getMaxStackSize())
					{
						if (inSlot.stackSize + itemStack.stackSize <= inSlot.getMaxStackSize())
						{
							ItemStack toSet = itemStack.copy();
							toSet.stackSize += inSlot.stackSize;

							inventory.setInventorySlotContents(slotID, toSet);
							return null;
						}
						else
						{
							int rejects = (inSlot.stackSize + itemStack.stackSize) - inSlot.getMaxStackSize();

							ItemStack toSet = itemStack.copy();
							toSet.stackSize = inSlot.getMaxStackSize();

							ItemStack remains = itemStack.copy();
							remains.stackSize = rejects;

							inventory.setInventorySlotContents(slotID, toSet);
							return remains;
						}
					}
				}
			}
		}

		return itemStack;
	}

	public static ItemStack takeTopItemFromInventory(IInventory inventory, int side)
	{
		if (!(inventory instanceof ISidedInventory))
		{
			for (int i = inventory.getSizeInventory() - 1; i >= 0; i--)
			{
				if (inventory.getStackInSlot(i) != null)
				{
					ItemStack toSend = inventory.getStackInSlot(i).copy();
					toSend.stackSize = 1;

					inventory.decrStackSize(i, 1);

					return toSend;
				}
			}
		}
		else
		{
			ISidedInventory sidedInventory = (ISidedInventory) inventory;
			int[] slots = sidedInventory.getAccessibleSlotsFromSide(side);

			if (slots != null)
			{
				for (int get = slots.length - 1; get >= 0; get--)
				{
					int slotID = slots[get];

					if (sidedInventory.getStackInSlot(slotID) != null)
					{
						ItemStack toSend = sidedInventory.getStackInSlot(slotID);
						toSend.stackSize = 1;

						if (sidedInventory.canExtractItem(slotID, toSend, side))
						{
							sidedInventory.decrStackSize(slotID, 1);

							return toSend;
						}
					}
				}
			}
		}

		return null;
	}
}
