package mekanism.common.tileentity;

import java.util.ArrayList;

import mekanism.api.EnumColor;
import mekanism.api.Object3D;
import mekanism.common.HashList;
import mekanism.common.IActiveState;
import mekanism.common.IRedstoneControl;
import mekanism.common.PacketHandler;
import mekanism.common.PacketHandler.Transmission;
import mekanism.common.block.BlockMachine.MachineType;
import mekanism.common.network.PacketTileEntity;
import mekanism.common.transporter.SlotInfo;
import mekanism.common.transporter.TransporterFilter;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.TransporterUtils;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;

import com.google.common.io.ByteArrayDataInput;

public class TileEntityLogisticalSorter extends TileEntityElectricBlock implements IRedstoneControl, IActiveState
{
	public HashList<TransporterFilter> filters = new HashList<TransporterFilter>();
	
	public RedstoneControl controlType = RedstoneControl.DISABLED;
	
	public final int MAX_DELAY = 10;
	
	public int delayTicks;
	
	public boolean isActive;
	
	public boolean clientActive;
	
	public TileEntityLogisticalSorter() 
	{
		super("LogisticalSorter", MachineType.LOGISTICAL_SORTER.baseEnergy);
		inventory = new ItemStack[1];
		doAutoSync = false;
	}
	
	@Override
	public void onUpdate()
	{
		super.onUpdate();
		
		if(!worldObj.isRemote)
		{
			delayTicks = Math.max(0, delayTicks-1);
			
			if(delayTicks == 8)
			{
				setActive(false);
			}
			
			if(MekanismUtils.canFunction(this) && delayTicks == 0)
			{
				TileEntity back = Object3D.get(this).getFromSide(ForgeDirection.getOrientation(facing).getOpposite()).getTileEntity(worldObj);
				TileEntity front = Object3D.get(this).getFromSide(ForgeDirection.getOrientation(facing)).getTileEntity(worldObj);
				
				if(back instanceof IInventory && front instanceof TileEntityLogisticalTransporter)
				{
					IInventory inventory = (IInventory)back;
					TileEntityLogisticalTransporter transporter = (TileEntityLogisticalTransporter)front;
					
					SlotInfo inInventory = TransporterUtils.takeItem(inventory, facing);
					
					EnumColor color = null;
					
					for(TransporterFilter filter : filters)
					{
						if(filter.canFilter(inInventory.itemStack))
						{
							color = filter.color;
							break;
						}
					}
					
					if(inInventory != null && inInventory.itemStack != null)
					{
						if(TransporterUtils.insert(this, transporter, inInventory.itemStack, color))
						{
							inventory.setInventorySlotContents(inInventory.slotID, null);
							setActive(true);
						}
						else {
							inventory.setInventorySlotContents(inInventory.slotID, inInventory.itemStack);
						}
					}
					
					delayTicks = 10;
				}
			}
			
			if(playersUsing.size() > 0)
			{
				PacketHandler.sendPacket(Transmission.CLIENTS_RANGE, new PacketTileEntity().setParams(Object3D.get(this), getGenericPacket(new ArrayList())), Object3D.get(this), 50D);
			}
		}
	}
	
    @Override
    public void writeToNBT(NBTTagCompound nbtTags)
    {
        super.writeToNBT(nbtTags);
        
        nbtTags.setInteger("controlType", controlType.ordinal());
        
        NBTTagList filterTags = new NBTTagList();
        
        for(TransporterFilter filter : filters)
        {
        	NBTTagCompound tagCompound = new NBTTagCompound();
        	filter.write(tagCompound);
        	filterTags.appendTag(tagCompound);
        }
        
        if(filterTags.tagCount() != 0)
        {
        	nbtTags.setTag("filters", filterTags);
        }
    }
    
    @Override
    public void readFromNBT(NBTTagCompound nbtTags)
    {
    	super.readFromNBT(nbtTags);
    	
    	controlType = RedstoneControl.values()[nbtTags.getInteger("controlType")];
    	
       	if(nbtTags.hasKey("filters"))
    	{
    		NBTTagList tagList = nbtTags.getTagList("filters");
    		
    		for(int i = 0; i < tagList.tagCount(); i++)
    		{
    			filters.add(TransporterFilter.readFromNBT((NBTTagCompound)tagList.tagAt(i)));
    		}
    	}
    }
	
	@Override
	public void handlePacketData(ByteArrayDataInput dataStream)
	{
		super.handlePacketData(dataStream);
		
		int type = dataStream.readInt();
		
		if(type == 0)
		{
			isActive = dataStream.readBoolean();
			controlType = RedstoneControl.values()[dataStream.readInt()];
			
			filters.clear();
			
			int amount = dataStream.readInt();
			
			for(int i = 0; i < amount; i++)
			{
				filters.add(TransporterFilter.readFromPacket(dataStream));
			}
			
			MekanismUtils.updateBlock(worldObj, xCoord, yCoord, zCoord);
		}
		else if(type == 1)
		{
			isActive = dataStream.readBoolean();
			controlType = RedstoneControl.values()[dataStream.readInt()];	
			
			MekanismUtils.updateBlock(worldObj, xCoord, yCoord, zCoord);
		}
		else if(type == 2)
		{
			filters.clear();
			
			int amount = dataStream.readInt();
			
			for(int i = 0; i < amount; i++)
			{
				filters.add(TransporterFilter.readFromPacket(dataStream));
			}
		}
	}
	
	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		super.getNetworkedData(data);
		
		data.add(0);
		
		data.add(isActive);
		data.add(controlType.ordinal());
		
		data.add(filters.size());
		
		for(TransporterFilter filter : filters)
		{
			filter.write(data);
		}
		
		return data;
	}
	
	public ArrayList getGenericPacket(ArrayList data)
	{
		super.getNetworkedData(data);
		
		data.add(1);
		
		data.add(isActive);
		data.add(controlType.ordinal());
		
		return data;
		
	}
	
	public ArrayList getFilterPacket(ArrayList data)
	{
		super.getNetworkedData(data);
		
		data.add(2);
		
		data.add(filters.size());
		
		for(TransporterFilter filter : filters)
		{
			filter.write(data);
		}
		
		return data;
	}
	
	@Override
	public boolean canExtractItem(int slotID, ItemStack itemstack, int side)
	{
		return false;
	}
	
	@Override
	public boolean isItemValidForSlot(int slotID, ItemStack itemstack)
	{
		return false;
	}
	
	@Override
	public int getInventoryStackLimit()
	{
		return 1;
	}
	
	@Override
	public int[] getAccessibleSlotsFromSide(int side) 
	{
		return new int[] {0};
	}

	@Override
	public void openChest()
	{
		if(!worldObj.isRemote)
		{
			PacketHandler.sendPacket(Transmission.CLIENTS_RANGE, new PacketTileEntity().setParams(Object3D.get(this), getFilterPacket(new ArrayList())), Object3D.get(this), 50D);
		}
	}
	
	@Override
	public RedstoneControl getControlType() 
	{
		return controlType;
	}

	@Override
	public void setControlType(RedstoneControl type) 
	{
		controlType = type;
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
    public boolean hasVisual()
    {
    	return true;
    }
}
