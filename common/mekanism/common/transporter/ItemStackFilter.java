package mekanism.common.transporter;

import java.util.ArrayList;

import com.google.common.io.ByteArrayDataInput;

import mekanism.common.util.TransporterUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class ItemStackFilter extends TransporterFilter
{
	public ItemStack itemStack;
	
	@Override
	public void write(NBTTagCompound nbtTags)
	{
		super.write(nbtTags);
		
		itemStack.writeToNBT(nbtTags);
	}
	
	@Override
	public void read(NBTTagCompound nbtTags)
	{
		super.read(nbtTags);
		
		itemStack = ItemStack.loadItemStackFromNBT(nbtTags);
	}
	
	@Override
	public void write(ArrayList data)
	{
		super.write(data);
		
		data.add(itemStack.itemID);
		data.add(itemStack.stackSize);
		data.add(itemStack.getItemDamage());
	}
	
	@Override
	public void read(ByteArrayDataInput dataStream)
	{
		super.read(dataStream);
		
		itemStack = new ItemStack(dataStream.readInt(), dataStream.readInt(), dataStream.readInt());
	}
}
