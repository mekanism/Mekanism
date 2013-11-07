package mekanism.common.transporter;

import java.util.ArrayList;

import com.google.common.io.ByteArrayDataInput;

import mekanism.common.util.TransporterUtils;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ForgeDirection;

public class ItemStackFilter extends TransporterFilter
{
	public boolean sizeMode;
	
	public int min;
	public int max;
	
	public ItemStack itemType;
	
	@Override
	public boolean canFilter(ItemStack itemStack)
	{
		if(itemStack == null)
		{
			return false;
		}
		
		if(sizeMode && max == 0)
		{
			return false;
		}
	
		return itemType.isItemEqual(itemStack) && (!sizeMode || itemStack.stackSize >= min);
	}
	
	@Override
	public InvStack getStackFromInventory(IInventory inv, ForgeDirection side)
	{
		if(sizeMode)
		{
			return TransporterUtils.takeDefinedItem(inv, side.ordinal(), itemType, min, max);
		}
		else {
			return TransporterUtils.takeTopItem(inv, side.ordinal());
		}
	}
	
	@Override
	public void write(NBTTagCompound nbtTags)
	{
		super.write(nbtTags);
		
		nbtTags.setInteger("type", 0);
		nbtTags.setBoolean("sizeMode", sizeMode);
		nbtTags.setInteger("min", min);
		nbtTags.setInteger("max", max);
		itemType.writeToNBT(nbtTags);
	}
	
	@Override
	protected void read(NBTTagCompound nbtTags)
	{
		super.read(nbtTags);
		
		sizeMode = nbtTags.getBoolean("sizeMode");
		min = nbtTags.getInteger("min");
		max = nbtTags.getInteger("max");
		
		itemType = ItemStack.loadItemStackFromNBT(nbtTags);
	}
	
	@Override
	public void write(ArrayList data)
	{
		data.add(0);
		
		super.write(data);
		
		data.add(sizeMode);
		data.add(min);
		data.add(max);
		
		data.add(itemType.itemID);
		data.add(itemType.stackSize);
		data.add(itemType.getItemDamage());
	}
	
	@Override
	protected void read(ByteArrayDataInput dataStream)
	{
		super.read(dataStream);
		
		sizeMode = dataStream.readBoolean();
		min = dataStream.readInt();
		max = dataStream.readInt();
		
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
		code = 31 * code + (sizeMode ? 1 : 0);
		code = 31 * code + min;
		code = 31 * code + max;
		return code;
	}
	
	@Override
	public boolean equals(Object filter)
	{
		return super.equals(filter) && filter instanceof ItemStackFilter && ((ItemStackFilter)filter).itemType.isItemEqual(itemType)
				&& ((ItemStackFilter)filter).sizeMode == sizeMode && ((ItemStackFilter)filter).min == min && ((ItemStackFilter)filter).max == max;
	}
	
	@Override
	public ItemStackFilter clone()
	{
		ItemStackFilter filter = new ItemStackFilter();
		filter.color = color;
		filter.itemType = itemType.copy();
		filter.sizeMode = sizeMode;
		filter.min = min;
		filter.max = max;
		
		return filter;
	}
}
