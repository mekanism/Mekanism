package mekanism.common.transporter;

import java.util.ArrayList;

import com.google.common.io.ByteArrayDataInput;

import mekanism.common.util.TransporterUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class ItemStackFilter extends TransporterFilter
{
	public ItemStack itemType;
	
	@Override
	public boolean canFilter(ItemStack itemStack)
	{
		return itemType.isItemEqual(itemStack);
	}
	
	@Override
	public void write(NBTTagCompound nbtTags)
	{
		super.write(nbtTags);
		
		nbtTags.setInteger("type", 0);
		itemType.writeToNBT(nbtTags);
	}
	
	@Override
	protected void read(NBTTagCompound nbtTags)
	{
		super.read(nbtTags);
		
		itemType = ItemStack.loadItemStackFromNBT(nbtTags);
	}
	
	@Override
	public void write(ArrayList data)
	{
		data.add(0);
		
		super.write(data);
		
		data.add(itemType.itemID);
		data.add(itemType.stackSize);
		data.add(itemType.getItemDamage());
	}
	
	@Override
	protected void read(ByteArrayDataInput dataStream)
	{
		super.read(dataStream);
		
		itemType = new ItemStack(dataStream.readInt(), dataStream.readInt(), dataStream.readInt());
	}
	
	@Override
	public int hashCode() 
	{
		int code = 1;
		code = 31 * code + super.hashCode();
		code = 31 * code + itemType.itemID;
		code = 31 * code + itemType.stackSize;
		code = 31 * code + itemType.getItemDamage();
		return code;
	}
	
	@Override
	public boolean equals(Object filter)
	{
		return super.equals(filter) && filter instanceof ItemStackFilter && ((ItemStackFilter)filter).itemType.isItemEqual(itemType);
	}
}
