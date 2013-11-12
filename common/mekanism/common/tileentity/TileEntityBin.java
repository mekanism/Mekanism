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
	public void onUpdate() {}
	
	@Override
	public void writeToNBT(NBTTagCompound nbtTags)
	{
		super.writeToNBT(nbtTags);
		
		nbtTags.setInteger("itemCount", itemCount);
		
		if(itemCount > 0)
		{
			nbtTags.setCompoundTag("itemType", itemType.writeToNBT(new NBTTagCompound()));
		}
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbtTags)
	{
		super.readFromNBT(nbtTags);
		
		itemCount = nbtTags.getInteger("itemCount");
		
		if(itemCount > 0)
		{
			itemType = ItemStack.loadItemStackFromNBT(nbtTags.getCompoundTag("itemType"));
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
		if(slotID == 1)
		{
			return null;
		}
		else if(slotID == 0)
		{
			int toRemove = Math.min(itemCount, amount);
			
			if(toRemove > 0)
			{
				ItemStack ret = itemType.copy();
				ret.stackSize = toRemove;
				
				setItemCount(itemCount-toRemove);
				
				return ret;
			}
		}
		
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
		if(itemType != null && itemstack != null && !itemstack.isItemEqual(itemType))
		{
			return;
		}
		
		if(i == 0)
		{
			if(itemCount == 0)
			{
				return;
			}
			
			if(itemstack == null)
			{
				setItemCount(itemCount - getStack().stackSize);
			}
			else {
				setItemCount(itemCount - (getStack().stackSize-itemstack.stackSize));
			}
		}
		else if(i == 1)
		{
			if(itemstack != null && itemstack.stackSize > 0)
			{
				if(itemType == null)
				{
					setItemType(itemstack);
				}
				
				setItemCount(itemCount + itemstack.stackSize);
			}
		}
	}
	
	public void setItemType(ItemStack stack)
	{
		if(stack == null)
		{
			itemType = null;
			return;
		}
		
		ItemStack ret = stack.copy();
		ret.stackSize = 1;
		itemType = ret;
	}
	
	public void setItemCount(int count)
	{
		itemCount = Math.max(0, count);
		
		if(itemCount == 0)
		{
			setItemType(null);
		}
	}

	@Override
	public String getInvName() 
	{
		return "Bin";
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
		return itemType != null && itemType.isItemEqual(itemstack);
	}
}