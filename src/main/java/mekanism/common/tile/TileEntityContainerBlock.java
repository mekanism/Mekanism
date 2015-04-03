package mekanism.common.tile;

import mekanism.common.Upgrade;
import mekanism.common.base.ISustainedInventory;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants.NBT;

public abstract class TileEntityContainerBlock extends TileEntityBasicBlock implements ISidedInventory, ISustainedInventory
{
	/** The inventory slot itemstacks used by this block. */
	public ItemStack[] inventory;

	/** The full name of this machine. */
	public String fullName;

	/**
	 * A simple tile entity with a container and facing state.
	 * @param name - full name of this tile entity
	 */
	public TileEntityContainerBlock(String name)
	{
		fullName = name;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbtTags)
	{
		super.readFromNBT(nbtTags);

		if(handleInventory())
		{
			NBTTagList tagList = nbtTags.getTagList("Items", NBT.TAG_COMPOUND);
			inventory = new ItemStack[getSizeInventory()];

			for(int tagCount = 0; tagCount < tagList.tagCount(); tagCount++)
			{
				NBTTagCompound tagCompound = tagList.getCompoundTagAt(tagCount);
				byte slotID = tagCompound.getByte("Slot");

				if(slotID >= 0 && slotID < getSizeInventory())
				{
					setInventorySlotContents(slotID, ItemStack.loadItemStackFromNBT(tagCompound));
				}
			}
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbtTags)
	{
		super.writeToNBT(nbtTags);

		if(handleInventory())
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

			nbtTags.setTag("Items", tagList);
		}
	}

	@Override
	public int getSizeInventory()
	{
		return inventory != null ? inventory.length : 0;
	}

	@Override
	public ItemStack getStackInSlot(int slotID)
	{
		return inventory != null ? inventory[slotID] : null;
	}

	@Override
	public ItemStack decrStackSize(int slotID, int amount)
	{
		if(getStackInSlot(slotID) != null)
		{
			ItemStack tempStack;

			if(getStackInSlot(slotID).stackSize <= amount)
			{
				tempStack = getStackInSlot(slotID);
				setInventorySlotContents(slotID, null);
				return tempStack;
			}
			else {
				tempStack = getStackInSlot(slotID).splitStack(amount);

				if(getStackInSlot(slotID).stackSize == 0)
				{
					setInventorySlotContents(slotID, null);
				}

				return tempStack;
			}
		}
		else {
			return null;
		}
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int slotID)
	{
		if(getStackInSlot(slotID) != null)
		{
			ItemStack tempStack = getStackInSlot(slotID);
			setInventorySlotContents(slotID, null);
			return tempStack;
		}
		else {
			return null;
		}
	}

	@Override
	public void setInventorySlotContents(int slotID, ItemStack itemstack)
	{
		inventory[slotID] = itemstack;

		if(itemstack != null && itemstack.stackSize > getInventoryStackLimit())
		{
			itemstack.stackSize = getInventoryStackLimit();
		}
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer)
	{
		return !isInvalid();
	}

	@Override
	public String getInventoryName()
	{
		return MekanismUtils.localize(getBlockType().getUnlocalizedName() + "." + fullName + ".name");
	}

	@Override
	public int getInventoryStackLimit()
	{
		return 64;
	}

	@Override
	public void openInventory() {}

	@Override
	public void closeInventory() {}

	@Override
	public boolean hasCustomInventoryName()
	{
		return true;
	}

	@Override
	public boolean isItemValidForSlot(int slotID, ItemStack itemstack)
	{
		return true;
	}

	@Override
	public boolean canInsertItem(int slotID, ItemStack itemstack, int side)
	{
		return isItemValidForSlot(slotID, itemstack);
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int side)
	{
		return InventoryUtils.EMPTY;
	}

	@Override
	public boolean canExtractItem(int slotID, ItemStack itemstack, int side)
	{
		return true;
	}

	@Override
	public void setInventory(NBTTagList nbtTags, Object... data)
	{
		if(nbtTags == null || nbtTags.tagCount() == 0 || !handleInventory())
		{
			return;
		}

		inventory = new ItemStack[getSizeInventory()];

		for(int slots = 0; slots < nbtTags.tagCount(); slots++)
		{
			NBTTagCompound tagCompound = (NBTTagCompound)nbtTags.getCompoundTagAt(slots);
			byte slotID = tagCompound.getByte("Slot");

			if(slotID >= 0 && slotID < inventory.length)
			{
				inventory[slotID] = ItemStack.loadItemStackFromNBT(tagCompound);
			}
		}
	}

	@Override
	public NBTTagList getInventory(Object... data)
	{
		NBTTagList tagList = new NBTTagList();

		if(handleInventory())
		{
			for(int slots = 0; slots < inventory.length; slots++)
			{
				if(inventory[slots] != null)
				{
					NBTTagCompound tagCompound = new NBTTagCompound();
					tagCompound.setByte("Slot", (byte)slots);
					inventory[slots].writeToNBT(tagCompound);
					tagList.appendTag(tagCompound);
				}
			}
		}

		return tagList;
	}

	public boolean handleInventory()
	{
		return true;
	}

	public void recalculateUpgradables(Upgrade upgradeType) {}
}
