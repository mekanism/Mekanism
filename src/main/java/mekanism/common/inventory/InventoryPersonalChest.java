package mekanism.common.inventory;

import mekanism.common.base.ISustainedInventory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class InventoryPersonalChest extends InventoryBasic
{
	public EntityPlayer entityPlayer;
	public ItemStack itemStack;

	public boolean reading;

	public InventoryPersonalChest(EntityPlayer player)
	{
		super("PersonalChest", false, 55);
		entityPlayer = player;

		read();
	}

	public InventoryPersonalChest(ItemStack stack)
	{
		super("PersonalChest", false, 55);
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
	}

	@Override
	public void closeInventory()
	{
		write();
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
			if (getStack().getItem() instanceof ISustainedInventory) {
                            ((ISustainedInventory)getStack().getItem()).setInventory(tagList, getStack());
                        } else {
                            System.out.println("Avoiding a server crash as : " + getStack().getItem().getClass().getName() + " is not a sustained inventory.");
                        }
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
