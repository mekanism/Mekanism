package mekanism.common.frequency;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import mekanism.api.Coord4D;
import mekanism.common.PacketHandler;
import net.minecraft.nbt.NBTTagCompound;

public class Frequency
{
	public String name;
	public String owner;
	
	public boolean valid = true;
	
	public boolean publicFreq;
	
	public Set<Coord4D> activeCoords = new HashSet<Coord4D>();
	
	public Frequency(String n, String o)
	{
		name = n;
		owner = o;
	}
	
	public Frequency(NBTTagCompound nbtTags)
	{
		read(nbtTags);
	}
	
	public Frequency(ByteBuf dataStream)
	{
		read(dataStream);
	}
	
	public Frequency setPublic(boolean isPublic)
	{
		publicFreq = isPublic;
		
		return this;
	}
	
	public boolean isPublic()
	{
		return publicFreq;
	}
	
	public boolean isPrivate()
	{
		return !publicFreq;
	}
	
	public Coord4D getClosestCoords(Coord4D coord)
	{
		Coord4D closest = null;
		
		for(Coord4D iterCoord : activeCoords)
		{
			if(iterCoord.equals(coord))
			{
				continue;
			}
			
			if(closest == null)
			{
				closest = iterCoord;
				continue;
			}
			
			if(coord.dimensionId != closest.dimensionId && coord.dimensionId == iterCoord.dimensionId)
			{
				closest = iterCoord;
				continue;
			}
			else if(coord.dimensionId == closest.dimensionId && coord.dimensionId != iterCoord.dimensionId)
			{
				continue;
			}
			else {
				if(coord.distanceTo(closest) > coord.distanceTo(iterCoord))
				{
					closest = iterCoord;
					continue;
				}
				else {
					continue;
				}
			}
		}
		
		return closest;
	}
	
	public void write(NBTTagCompound nbtTags)
	{
		nbtTags.setString("name", name);
		nbtTags.setString("owner", owner);
		nbtTags.setBoolean("publicFreq", publicFreq);
	}

	protected void read(NBTTagCompound nbtTags)
	{
		name = nbtTags.getString("name");
		owner = nbtTags.getString("owner");
		publicFreq = nbtTags.getBoolean("publicFreq");
	}

	public void write(ArrayList data)
	{
		data.add(name);
		data.add(owner);
		data.add(publicFreq);
	}

	protected void read(ByteBuf dataStream)
	{
		name = PacketHandler.readString(dataStream);
		owner = PacketHandler.readString(dataStream);
		publicFreq = dataStream.readBoolean();
	}
	
	@Override
	public int hashCode()
	{
		int code = 1;
		code = 31 * code + name.hashCode();
		code = 31 * code + owner.hashCode();
		code = 31 * code + (publicFreq ? 1 : 0);
		return code;
	}

	@Override
	public boolean equals(Object obj)
	{
		return obj instanceof Frequency && ((Frequency)obj).name.equals(name) 
				&& ((Frequency)obj).owner.equals(owner) && ((Frequency)obj).publicFreq == publicFreq;
	}
}