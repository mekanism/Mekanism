package mekanism.common.inventory;

import mekanism.common.base.IElectricChest;
import mekanism.common.base.ISustainedInventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class InventoryElectricChest extends InventoryBasic
{
	public EntityPlayer entityPlayer;
	public ItemStack itemStack;

	public boolean reading;

	public InventoryElectricChest(EntityPlayer player)
	{
		super("Electric Chest", false, 55);
		entityPlayer = player;

		read();
	}

	public InventoryElectricChest(ItemStack stack)
	{
		super("Electric Chest", false, 55);
		itemStack = stack;

		read();
	}

	@Override
	public void markDirty()
	{
		super.markDirty();

		if(!reading)
		{
			write();
		}
	}

	@Override
	public void openInventory()
	{
		read();
		
		if(getStack() != null)
		{
			((IElectricChest)getStack().getItem()).setOpen(getStack(), true);
		}
	}

	@Override
	public void closeInventory()
	{
		write();
		
		if(getStack() != null)
		{
			((IElectricChest)getStack().getItem()).setOpen(getStack(), false);
		}
	}

	public void write()
	{
		NBTTagList tagList = new NBTTagList();

		for(int slotCount = 0; slotCount < getSizeInventory(); slotCount++)
		{
			if(getStackInSlot(slotCount) != null)
			{
				NBTTagCompound tagCompound = new NBTTagCompound();
				tagCompound.setByte("Slot", (byte)slotCount);
				getStackInSlot(slotCount).writeToNBT(tagCompound);
				tagList.appendTag(tagCompound);
			}
		}

		if(getStack() != null)
		{
			((ISustainedInventory)getStack().getItem()).setInventory(tagList, getStack());
		}
	}

	public void read()
	{
		if(reading)
		{
			return;
		}

		reading = true;

		NBTTagList tagList = ((ISustainedInventory)getStack().getItem()).getInventory(getStack());

		if(tagList != null)
		{
			for(int tagCount = 0; tagCount < tagList.tagCount(); tagCount++)
			{
				NBTTagCompound tagCompound = (NBTTagCompound)tagList.getCompoundTagAt(tagCount);
				byte slotID = tagCompound.getByte("Slot");

				if(slotID >= 0 && slotID < getSizeInventory())
				{
					setInventorySlotContents(slotID, ItemStack.loadItemStackFromNBT(tagCompound));
				}
			}
		}

		reading = false;
	}

	public ItemStack getStack()
	{
		return itemStack != null ? itemStack : entityPlayer.getCurrentEquippedItem();
	}
}
