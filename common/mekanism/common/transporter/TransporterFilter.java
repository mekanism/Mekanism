package mekanism.common.transporter;

import java.util.ArrayList;

import mekanism.api.EnumColor;
import mekanism.common.Teleporter.Code;
import mekanism.common.util.TransporterUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import com.google.common.io.ByteArrayDataInput;

public class TransporterFilter 
{
	public EnumColor color;
	
	public boolean canFilter(ItemStack itemStack)
	{
		return false;
	}
	
	public void write(NBTTagCompound nbtTags)
	{
		nbtTags.setInteger("color", TransporterUtils.colors.indexOf(color));
	}
	
	public void read(NBTTagCompound nbtTags)
	{
		color = TransporterUtils.colors.get(nbtTags.getInteger("color"));
	}
	
	public void write(ArrayList data)
	{
		data.add(TransporterUtils.colors.indexOf(color));
	}
	
	public void read(ByteArrayDataInput dataStream)
	{
		color = TransporterUtils.colors.get(dataStream.readInt());
	}
	
	public static TransporterFilter readFromNBT(NBTTagCompound nbtTags)
	{
		TransporterFilter filter = new TransporterFilter();
		filter.read(nbtTags);
		
		return filter;
	}
	
	public static TransporterFilter readFromPacket(ByteArrayDataInput dataStream)
	{
		TransporterFilter filter = new TransporterFilter();
		filter.read(dataStream);
		
		return filter;
	}
	
	@Override
	public int hashCode() 
	{
		int code = 1;
		code = 31 * code + color.ordinal();
		return code;
	}
	
	@Override
	public boolean equals(Object filter)
	{
		return filter instanceof TransporterFilter && ((TransporterFilter)filter).color == color;
	}
}
