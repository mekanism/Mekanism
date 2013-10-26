package mekanism.common.transporter;

import java.util.ArrayList;

import com.google.common.io.ByteArrayDataInput;

import mekanism.common.util.MekanismUtils;
import mekanism.common.util.TransporterUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class OreDictFilter extends TransporterFilter
{
	public String oreDictName;
	
	@Override
	public boolean canFilter(ItemStack itemStack)
	{
		return MekanismUtils.oreDictCheck(itemStack, oreDictName);
	}
	
	@Override
	public void write(NBTTagCompound nbtTags)
	{
		super.write(nbtTags);
		
		nbtTags.setString("oreDictName", oreDictName);
	}
	
	@Override
	public void read(NBTTagCompound nbtTags)
	{
		super.read(nbtTags);
		
		oreDictName = nbtTags.getString("oreDictName");
	}
	
	@Override
	public void write(ArrayList data)
	{
		super.write(data);
		
		data.add(oreDictName);
	}
	
	@Override
	public void read(ByteArrayDataInput dataStream)
	{
		super.read(dataStream);
		
		oreDictName = dataStream.readUTF();
	}
	
	@Override
	public int hashCode() 
	{
		int code = 1;
		code = 31 * code + super.hashCode();
		code = 31 * code + oreDictName.hashCode();
		return code;
	}
	
	@Override
	public boolean equals(Object filter)
	{
		return super.equals(filter) && filter instanceof OreDictFilter && ((OreDictFilter)filter).oreDictName.equals(oreDictName);
	}
}
