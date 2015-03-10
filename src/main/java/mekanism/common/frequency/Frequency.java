package mekanism.common.frequency;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;

import mekanism.common.PacketHandler;
import net.minecraft.nbt.NBTTagCompound;

public class Frequency
{
	public String name;
	public String owner;
	
	public Frequency(String n, String o)
	{
		name = n;
		owner = o;
	}
	
	public void write(NBTTagCompound nbtTags)
	{
		nbtTags.setString("name", name);
		nbtTags.setString("owner", owner);
	}

	protected void read(NBTTagCompound nbtTags)
	{
		name = nbtTags.getString("name");
		owner = nbtTags.getString("owner");
	}

	public void write(ArrayList data)
	{
		data.add(name);
		data.add(owner);
	}

	protected void read(ByteBuf dataStream)
	{
		name = PacketHandler.readString(dataStream);
		owner = PacketHandler.readString(dataStream);
	}
	
	@Override
	public int hashCode()
	{
		int code = 1;
		code = 31 * code + name.hashCode();
		return code;
	}

	@Override
	public boolean equals(Object obj)
	{
		return obj instanceof Frequency && ((Frequency)obj).name.equals(name);
	}
}