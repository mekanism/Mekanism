package mekanism.common.tileentity;

import java.util.ArrayList;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import com.google.common.io.ByteArrayDataInput;

public class TileEntityBin extends TileEntityBasicBlock implements ISidedInventory
{
	public int itemCount;
	public ItemStack itemType;
	
	public ItemStack getStack()
	{
		if(itemCount > 0)
		{
			ItemStack ret = itemType.copy();
			ret.stackSize = Math.min(64, itemCount);
			
			return ret;
		}
		
		return null;
	}
	
	@Override
	public void onUpdate()
	{
		
	}
	
	@Override
	public void writeToNBT(NBTTagCompound nbtTags)
	{
		super.writeToNBT(nbtTags);
		
		nbtTags.setInteger("itemCount", itemCount);
		
		if(itemCount > 0)
		{
			itemType.writeToNBT(nbtTags);
		}
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbtTags)
	{
		super.readFromNBT(nbtTags);
		
		itemCount = nbtTags.getInteger("itemCount");
		
		if(itemCount > 0)
		{
			itemType = ItemStack.loadItemStackFromNBT(nbtTags);
		}
	}
	
	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		super.getNetworkedData(data);
		
		data.add(itemCount);
		
		if(itemCount > 0)
		{
			data.add(itemType.itemID);
			data.add(itemType.getItemDamage());
		}
		
		return data;
	}
	
	@Override
	public void handlePacketData(ByteArrayDataInput dataStream)
	{
		super.handlePacketData(dataStream);
		
		itemCount = dataStream.readInt();
		
		if(itemCount > 0)
		{
			itemType = new ItemStack(dataStream.readInt(), 0, dataStream.readInt());
		}
	}
	
	@Override
	public ItemStack getStackInSlot(int slotID) 
	{
		if(slotID == 1)
		{
			return null;
		}
		else {
			return getStack();
		}
	}
	
	@Override
    public ItemStack decrStackSize(int slotID, int amount)
    {
		return null;
    }
	
	@Override
    public ItemStack getStackInSlotOnClosing(int slotID)
    {
		return getStackInSlot(slotID);
    }

	@Override
	public int getSizeInventory() 
	{
		return 1;
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack) 
	{
		
	}

	@Override
	public String getInvName() 
	{
		return null;
	}

	@Override
	public boolean isInvNameLocalized() 
	{
		return true;
	}

	@Override
	public int getInventoryStackLimit() 
	{
		return 64;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer) 
	{
		return true;
	}

	@Override
	public void openChest() {}

	@Override
	public void closeChest() {}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack) 
	{
		return itemType == null || itemType.isItemEqual(itemstack);
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int side) 
	{
		if(side == 1)
		{
			return new int[] {1};
		}
		else {
			return new int[] {0};		
		}
	}

	@Override
	public boolean canInsertItem(int i, ItemStack itemstack, int j) 
	{
		return isItemValidForSlot(i, itemstack);
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemstack, int j)
	{
		return true;
	}
}