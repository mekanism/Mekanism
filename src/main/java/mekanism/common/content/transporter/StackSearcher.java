package mekanism.common.content.transporter;

import mekanism.api.util.StackUtils;
import mekanism.common.util.InventoryUtils;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;

public class StackSearcher
{
	public int i;
	public int[] slots;
	public IInventory theInventory;
	public ForgeDirection side;

	public StackSearcher(IInventory inventory, ForgeDirection direction)
	{
		theInventory = InventoryUtils.checkChestInv(inventory);
		side = direction;
		if(!(theInventory instanceof ISidedInventory))
		{
			i = inventory.getSizeInventory();
		}
		else
		{
			slots = ((ISidedInventory)theInventory).getAccessibleSlotsFromSide(side.getOpposite().ordinal());
			if(slots != null)
			{
				i = slots.length;
			}
		}
	}

	public InvStack takeTopStack(Finder id)
	{
		if(!(theInventory instanceof ISidedInventory))
		{
			for(i = i - 1; i >= 0; i--)
			{
				if(theInventory.getStackInSlot(i) != null && id.modifies(theInventory.getStackInSlot(i)))
				{
					ItemStack toSend = theInventory.getStackInSlot(i).copy();
					return new InvStack(theInventory, i, toSend);
				}
			}
		}
		else {
			if(slots != null && slots.length != 0)
			{
				for(i = i - 1; i >= 0; i--)
				{
					int slotID = slots[i];

					if(theInventory.getStackInSlot(slotID) != null && id.modifies(theInventory.getStackInSlot(slotID)))
					{
						ItemStack toSend = theInventory.getStackInSlot(slotID);

						if(((ISidedInventory)theInventory).canExtractItem(slotID, toSend, side.getOpposite().ordinal()))
						{
							return new InvStack(theInventory, slotID, toSend);
						}
					}
				}
			}
		}

		return null;
	}

	public InvStack takeDefinedItem(ItemStack type, int min, int max)
	{
		InvStack ret = new InvStack(theInventory);

		if(!(theInventory instanceof ISidedInventory))
		{
			for(i = i - 1; i >= 0; i--)
			{
				if(theInventory.getStackInSlot(i) != null && StackUtils.equalsWildcard(theInventory.getStackInSlot(i), type))
				{
					ItemStack stack = theInventory.getStackInSlot(i);
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
			ISidedInventory sidedInventory = (ISidedInventory)theInventory;
			int[] slots = sidedInventory.getAccessibleSlotsFromSide(side.getOpposite().ordinal());

			if(slots != null && slots.length != 0)
			{
				for(i = i - 1; i >= 0; i--)
				{
					int slotID = slots[i];

					if(sidedInventory.getStackInSlot(slotID) != null && StackUtils.equalsWildcard(theInventory.getStackInSlot(slotID), type))
					{
						ItemStack stack = sidedInventory.getStackInSlot(slotID);
						int current = ret.getStack() != null ? ret.getStack().stackSize : 0;

						if(current+stack.stackSize <= max)
						{
							ItemStack copy = stack.copy();

							if(sidedInventory.canExtractItem(slotID, copy, side.getOpposite().ordinal()))
							{
								ret.appendStack(slotID, copy);
							}
						}
						else {
							ItemStack copy = stack.copy();

							if(sidedInventory.canExtractItem(slotID, copy, side.getOpposite().ordinal()))
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



}
