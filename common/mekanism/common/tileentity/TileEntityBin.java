package mekanism.common.tileentity;

import java.util.ArrayList;

import com.google.common.io.ByteArrayDataInput;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class TileEntityBin extends TileEntityContainerBlock
{
	public int itemCount;
	public ItemStack itemType;
	
	public TileEntityBin() 
	{
		super("Bin");
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
}