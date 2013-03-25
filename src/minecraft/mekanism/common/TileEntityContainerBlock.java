package mekanism.common;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.ISidedInventory;

public abstract class TileEntityContainerBlock extends TileEntityBasicBlock implements ISidedInventory, IInventory
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
        
        NBTTagList tagList = nbtTags.getTagList("Items");
        inventory = new ItemStack[getSizeInventory()];

        for(int slots = 0; slots < tagList.tagCount(); slots++)
        {
            NBTTagCompound tagCompound = (NBTTagCompound)tagList.tagAt(slots);
            byte slotID = tagCompound.getByte("Slot");

            if(slotID >= 0 && slotID < inventory.length)
            {
                inventory[slotID] = ItemStack.loadItemStackFromNBT(tagCompound);
            }
        }
    }

	@Override
    public void writeToNBT(NBTTagCompound nbtTags)
    {
        super.writeToNBT(nbtTags);
        
        NBTTagList tagList = new NBTTagList();

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

        nbtTags.setTag("Items", tagList);
    }
	
	@Override
	public int getStartInventorySide(ForgeDirection side) 
	{
        return 0;
	}

	@Override
	public int getSizeInventorySide(ForgeDirection side)
	{
		return 0;
	}

	@Override
	public int getSizeInventory() 
	{
		return inventory.length;
	}

	@Override
	public ItemStack getStackInSlot(int slotID) 
	{
		return inventory[slotID];
	}

	@Override
    public ItemStack decrStackSize(int slotID, int amount)
    {
        if(inventory[slotID] != null)
        {
            ItemStack tempStack;

            if(inventory[slotID].stackSize <= amount)
            {
                tempStack = inventory[slotID];
                inventory[slotID] = null;
                return tempStack;
            }
            else {
                tempStack = inventory[slotID].splitStack(amount);

                if(inventory[slotID].stackSize == 0)
                {
                    inventory[slotID] = null;
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
        if (inventory[slotID] != null)
        {
            ItemStack var2 = inventory[slotID];
            inventory[slotID] = null;
            return var2;
        }
        else
        {
            return null;
        }
    }

	@Override
    public void setInventorySlotContents(int slotID, ItemStack itemstack)
    {
        inventory[slotID] = itemstack;

        if (itemstack != null && itemstack.stackSize > getInventoryStackLimit())
        {
            itemstack.stackSize = getInventoryStackLimit();
        }
    }
	
	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer)
	{
		return worldObj.getBlockTileEntity(xCoord, yCoord, zCoord) != this ? false : entityplayer.getDistanceSq(xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D) <= 64.0D;
	}
	
	@Override
	public String getInvName()
	{
		return fullName;
	}
	
	@Override
	public int getInventoryStackLimit() 
	{
		return 64;
	}
	
	@Override
	public void openChest()
	{
		playersUsing++;
	}

	@Override
	public void closeChest()
	{
		playersUsing--;
	}
	
	@Override
	public boolean isInvNameLocalized()
	{
		return true;
	}

	@Override
	public boolean isStackValidForSlot(int i, ItemStack itemstack)
	{
		return false;
	}
}
