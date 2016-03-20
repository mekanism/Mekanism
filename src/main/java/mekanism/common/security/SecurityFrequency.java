package mekanism.common.security;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;

import mekanism.common.HashList;
import mekanism.common.PacketHandler;
import mekanism.common.frequency.Frequency;
import mekanism.common.security.ISecurity.SecurityMode;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraftforge.common.util.Constants.NBT;

public class SecurityFrequency extends Frequency
{
	public boolean override;
	
	public HashList<String> trusted = new HashList<String>();
	
	public SecurityMode securityMode = SecurityMode.PUBLIC;
	
	public SecurityFrequency(String o)
	{
		super("Security", o);
	}
	
	public SecurityFrequency(NBTTagCompound nbtTags)
	{
		super(nbtTags);
	}
	
	public SecurityFrequency(ByteBuf dataStream)
	{
		super(dataStream);
	}
	
	@Override
	public void write(NBTTagCompound nbtTags)
	{
		super.write(nbtTags);
		
		nbtTags.setBoolean("override", override);
		nbtTags.setInteger("securityMode", securityMode.ordinal());
		
		if(!trusted.isEmpty())
		{
			NBTTagList trustedList = new NBTTagList();
			
			for(String s : trusted)
			{
				trustedList.appendTag(new NBTTagString(s));
			}
			
			nbtTags.setTag("trusted", trustedList);
		}
	}

	@Override
	protected void read(NBTTagCompound nbtTags)
	{
		super.read(nbtTags);
		
		override = nbtTags.getBoolean("override");
		securityMode = SecurityMode.values()[nbtTags.getInteger("securityMode")];
		
		if(nbtTags.hasKey("trusted"))
		{
			NBTTagList trustedList = nbtTags.getTagList("trusted", NBT.TAG_STRING);
			
			for(int i = 0; i < trustedList.tagCount(); i++)
			{
				trusted.add(trustedList.getStringTagAt(i));
			}
		}
	}

	@Override
	public void write(ArrayList data)
	{
		super.write(data);
		
		data.add(override);
		data.add(securityMode.ordinal());
		
		data.add(trusted.size());
		
		for(String s : trusted)
		{
			data.add(trusted);
		}
	}

	@Override
	protected void read(ByteBuf dataStream)
	{
		super.read(dataStream);
		
		override = dataStream.readBoolean();
		securityMode = SecurityMode.values()[dataStream.readInt()];
		
		trusted.clear();
		int size = dataStream.readInt();
		
		for(int i = 0; i < size; i++)
		{
			trusted.add(PacketHandler.readString(dataStream));
		}
	}
}
