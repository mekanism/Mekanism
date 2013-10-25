package mekanism.common.tileentity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import mekanism.common.IRedstoneControl;
import mekanism.common.IRedstoneControl.RedstoneControl;
import mekanism.common.block.BlockMachine.MachineType;
import mekanism.common.transporter.TransporterFilter;
import mekanism.common.util.MekanismUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import com.google.common.io.ByteArrayDataInput;

public class TileEntityLogisticalSorter extends TileEntityElectricBlock implements IRedstoneControl
{
	public Set<TransporterFilter> filters = new HashSet<TransporterFilter>();
	
	public RedstoneControl controlType = RedstoneControl.DISABLED;
	
	public TileEntityLogisticalSorter() 
	{
		super("LogisticalSorter", MachineType.LOGISTICAL_SORTER.baseEnergy);
		inventory = new ItemStack[1];
	}
	
	@Override
	public void onUpdate()
	{
		super.onUpdate();
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
		
		controlType = RedstoneControl.values()[dataStream.readInt()];
		
		MekanismUtils.updateBlock(worldObj, xCoord, yCoord, zCoord);
	}
	
	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		super.getNetworkedData(data);
		
		data.add(controlType.ordinal());
		
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
	public RedstoneControl getControlType() 
	{
		return controlType;
	}

	@Override
	public void setControlType(RedstoneControl type) 
	{
		controlType = type;
	}
}
