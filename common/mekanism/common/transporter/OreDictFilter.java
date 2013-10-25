package mekanism.common.transporter;

import java.util.ArrayList;

import com.google.common.io.ByteArrayDataInput;

import mekanism.common.util.TransporterUtils;
import net.minecraft.nbt.NBTTagCompound;

public class OreDictFilter extends TransporterFilter
{
	public String oreDictName;
	
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
}
