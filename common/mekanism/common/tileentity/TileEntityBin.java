package mekanism.common.tileentity;

import java.util.ArrayList;

import mekanism.api.Object3D;
import mekanism.common.IActiveState;
import mekanism.common.PacketHandler;
import mekanism.common.PacketHandler.Transmission;
import mekanism.common.network.PacketTileEntity;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.TransporterUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;

import com.google.common.io.ByteArrayDataInput;

public class TileEntityBin extends TileEntityBasicBlock implements ISidedInventory, IActiveState
{
	public boolean isActive;
	
	public boolean clientActive;
	
	public final int MAX_DELAY = 10;
	
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
		if(isValid(stack))
		{
			if(itemType == null)
			{
				setItemType(stack);
			}
			
			setItemCount(itemCount + stack.stackSize);
			onInventoryChanged();
		}
		
		return null;
	}
	
	public ItemStack removeStack()
	{
		ItemStack stack = getStack();
		
		if(stack == null)
		{
			return null;
		}
		
		setItemCount(itemCount - stack.stackSize);
		onInventoryChanged();
		
		return stack;
	}
	
	@Override
	public void onUpdate() 
	{
		if(!worldObj.isRemote)
		{
			delayTicks = Math.max(0, delayTicks-1);
			
			if(getStack() != null && isActive && delayTicks == 0)
			{
				TileEntity tile = Object3D.get(this).getFromSide(ForgeDirection.getOrientation(0)).getTileEntity(worldObj);
				
				if(tile instanceof TileEntityLogisticalTransporter)
				{
					TileEntityLogisticalTransporter transporter = (TileEntityLogisticalTransporter)tile;
					
					if(TransporterUtils.insert(this, transporter, getStack(), null))
					{
						setInventorySlotContents(0, null);
					}
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
		return 1;
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
		
		onInventoryChanged();
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
		return i == 1 ? isValid(itemstack) : false;
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
}