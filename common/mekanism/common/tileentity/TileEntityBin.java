package mekanism.common.tileentity;

import java.util.ArrayList;

import mekanism.common.IActiveState;
import mekanism.common.Object3D;
import mekanism.common.PacketHandler;
import mekanism.common.PacketHandler.Transmission;
import mekanism.common.item.ItemBlockBasic;
import mekanism.common.network.PacketTileEntity;
import mekanism.common.transporter.TransporterManager;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.TransporterUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import powercrystals.minefactoryreloaded.api.IDeepStorageUnit;

import com.google.common.io.ByteArrayDataInput;

public class TileEntityBin extends TileEntityBasicBlock implements ISidedInventory, IActiveState, IDeepStorageUnit
{
	public boolean isActive;
	
	public boolean clientActive;
	
	public final int MAX_DELAY = 10;
	
	public int addTicks = 0;
	
	public int delayTicks;
	
	public int itemCount;
	
	public final int MAX_STORAGE = 4096;
	
	public ItemStack itemType;
	
	public ItemStack getStack()
	{
		if(itemCount > 0)
		{
			ItemStack ret = itemType.copy();
			ret.stackSize = Math.min(itemType.getMaxStackSize(), itemCount);
			
			return ret;
		}
		
		return null;
	}
	
	public ItemStack getInsertStack()
	{
		int remain = MAX_STORAGE-itemCount;
		
		if(itemType == null)
		{
			return null;
		}
		
		if(remain >= itemType.getMaxStackSize())
		{
			return null;
		}
		else {
			ItemStack ret = itemType.copy();
			ret.stackSize = itemType.getMaxStackSize()-remain;
			
			return ret;
		}
	}
	
	public boolean isValid(ItemStack stack)
	{
		if(stack == null || stack.stackSize <= 0)
		{
			return false;
		}
		
		if(stack.isItemStackDamageable() && stack.isItemDamaged())
		{
			return false;
		}
		
		if(stack.getItem() instanceof ItemBlockBasic && stack.getItemDamage() == 6)
		{
			return false;
		}
		
		if(itemType == null)
		{
			return true;
		}
		
		if(!stack.isItemEqual(itemType) || !ItemStack.areItemStackTagsEqual(stack, itemType))
		{
			return false;
		}
		
		return true;
	}
	
	public ItemStack add(ItemStack stack)
	{
		if(isValid(stack) && itemCount != MAX_STORAGE)
		{
			if(itemType == null)
			{
				setItemType(stack);
			}
			
			if(itemCount + stack.stackSize <= MAX_STORAGE)
			{
				setItemCount(itemCount + stack.stackSize);
				return null;
			}
			else {
				ItemStack rejects = itemType.copy();
				rejects.stackSize = (itemCount+stack.stackSize) - MAX_STORAGE;
				
				setItemCount(MAX_STORAGE);
				
				return rejects;
			}
		}
		
		return stack;
	}
	
	public ItemStack removeStack()
	{
		if(itemCount == 0)
		{
			return null;
		}
		
		return remove(getStack().stackSize);
	}
	
	public ItemStack remove(int amount)
	{
		if(itemCount == 0)
		{
			return null;
		}
		
		ItemStack ret = itemType.copy();
		ret.stackSize = Math.min(Math.min(amount, itemType.getMaxStackSize()), itemCount);
		
		setItemCount(itemCount - ret.stackSize);
		
		return ret;
	}
	
	@Override
	public void onUpdate() 
	{
		if(!worldObj.isRemote)
		{
			addTicks = Math.max(0, addTicks-1);
			delayTicks = Math.max(0, delayTicks-1);
			
			if(getStack() != null && isActive && delayTicks == 0)
			{
				TileEntity tile = Object3D.get(this).getFromSide(ForgeDirection.getOrientation(0)).getTileEntity(worldObj);
				
				if(tile instanceof TileEntityLogisticalTransporter)
				{
					TileEntityLogisticalTransporter transporter = (TileEntityLogisticalTransporter)tile;
					
					ItemStack rejects = TransporterUtils.insert(this, transporter, getStack(), null, true, 0);
					
					if(TransporterManager.didEmit(getStack(), rejects))
					{
						setInventorySlotContents(0, rejects);
					}
				}
				else if(tile instanceof IInventory)
				{
					setInventorySlotContents(0, InventoryUtils.putStackInInventory((IInventory)tile, getStack(), 0, false));
				}
			}
		}
	}
	
	@Override
	public void writeToNBT(NBTTagCompound nbtTags)
	{
		super.writeToNBT(nbtTags);
		
		nbtTags.setBoolean("isActive", isActive);
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
		
		isActive = nbtTags.getBoolean("isActive");
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
		
		data.add(isActive);
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
		
		isActive = dataStream.readBoolean();
		itemCount = dataStream.readInt();
		
		if(itemCount > 0)
		{
			itemType = new ItemStack(dataStream.readInt(), 0, dataStream.readInt());
		}
		
		MekanismUtils.updateBlock(worldObj, xCoord, yCoord, zCoord);
	}
	
	@Override
	public ItemStack getStackInSlot(int slotID) 
	{
		if(slotID == 1)
		{
			return getInsertStack();
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
		return 2;
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack) 
	{
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
			if(isValid(itemstack))
			{
				add(itemstack);
			}
		}
	}
	
	@Override
	public void onInventoryChanged()
	{
		super.onInventoryChanged();
		
		if(!worldObj.isRemote)
		{
			PacketHandler.sendPacket(Transmission.ALL_CLIENTS, new PacketTileEntity().setParams(Object3D.get(this), getNetworkedData(new ArrayList())));
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
		
		onInventoryChanged();
	}

	@Override
	public String getInvName() 
	{
		return MekanismUtils.localize("tile.BasicBlock.Bin.name");
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
		return i == 1 ? isValid(itemstack) : false;
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int side) 
	{
		if(side == 1)
		{
			return new int[] {1};
		}
		else if(side == 0)
		{
			return new int[] {0};		
		}
		
		return new int[0];
	}

	@Override
	public boolean canInsertItem(int i, ItemStack itemstack, int j) 
	{
		return isItemValidForSlot(i, itemstack);
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemstack, int j)
	{
		return i == 0 ? isValid(itemstack) : false;
	}
	
	@Override
	public boolean canSetFacing(int facing)
	{
		return facing != 0 && facing != 1;
	}

	@Override
    public void setActive(boolean active)
    {
    	isActive = active;
    	
    	if(clientActive != active)
    	{
    		PacketHandler.sendPacket(Transmission.ALL_CLIENTS, new PacketTileEntity().setParams(Object3D.get(this), getNetworkedData(new ArrayList())));
    		
    		clientActive = active;
    	}
    }
    
    @Override
    public boolean getActive()
    {
    	return isActive;
    }

	@Override
	public boolean renderUpdate() 
	{
		return true;
	}

	@Override
	public boolean lightUpdate() 
	{
		return true;
	}

	@Override
	public ItemStack getStoredItemType()
	{
		return MekanismUtils.size(itemType, itemCount);
	}

	@Override
	public void setStoredItemCount(int amount)
	{
		if(amount == 0)
		{
			setStoredItemType(null, 0);
		}
		
		itemCount = amount;
	}

	@Override
	public void setStoredItemType(ItemStack type, int amount)
	{
		itemType = type;
		itemCount = amount;
	}

	@Override
	public int getMaxStoredCount()
	{
		return MAX_STORAGE;
	}
}