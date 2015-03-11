package mekanism.common.frequency;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import mekanism.api.Coord4D;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import net.minecraftforge.common.util.Constants.NBT;

public class FrequencyManager
{
	public static boolean loaded;
	
	private static Set<FrequencyManager> managers = new HashSet<FrequencyManager>();
	
	private Set<Frequency> frequencies = new HashSet<Frequency>();
	
	private FrequencyDataHandler dataHandler;
	
	private String owner;
	
	private Class<? extends Frequency> frequencyClass;
	
	public FrequencyManager(Class c)
	{
		frequencyClass = c;
		managers.add(this);
	}
	
	public FrequencyManager(Class c, String s)
	{
		this(c);
		
		owner = s;
	}
	
	public static void load(World world)
	{
		loaded = true;
		
		for(FrequencyManager manager : managers)
		{
			manager.createOrLoad(world);
		}
	}
	
	public Frequency update(String user, Coord4D coord, Frequency freq)
	{
		for(Frequency iterFreq : frequencies)
		{
			if(freq.equals(iterFreq))
			{
				iterFreq.activeCoords.add(coord);
				return iterFreq;
			}
		}
		
		return null;
	}
	
	public void deactivate(Coord4D coord)
	{
		for(Frequency freq : frequencies)
		{
			freq.activeCoords.remove(coord);
		}
	}
	
	public Frequency validateFrequency(String user, Coord4D coord, Frequency freq)
	{
		for(Frequency iterFreq : frequencies)
		{
			if(freq.equals(iterFreq))
			{
				iterFreq.activeCoords.add(coord);
				return iterFreq;
			}
		}
		
		if(user.equals(freq.owner))
		{
			freq.activeCoords.add(coord);
			frequencies.add(freq);
			return freq;
		}
		
		return null;
	}
	
	public void createOrLoad(World world)
	{
		if(dataHandler == null)
		{
			dataHandler = (FrequencyDataHandler)world.perWorldStorage.loadData(FrequencyDataHandler.class, getName());
			
			if(dataHandler == null)
			{
				dataHandler = new FrequencyDataHandler(getName());
				dataHandler.setManager(this);
				world.perWorldStorage.setData(getName(), dataHandler);
			}
			else {
				dataHandler.setManager(this);
				dataHandler.syncManager();
			}
		}
	}
	
	public Set<Frequency> getFrequencies()
	{
		return frequencies;
	}
	
	public void addFrequency(Frequency freq)
	{
		frequencies.add(freq);
	}
	
	public boolean containsFrequency(Frequency freq)
	{
		return frequencies.contains(freq);
	}
	
	public void writeFrequencies(ArrayList data)
	{
		data.add(frequencies.size());
		
		for(Frequency freq : frequencies)
		{
			freq.write(data);
		}
	}
	
	public Set<Frequency> readFrequencies(ByteBuf dataStream)
	{
		Set<Frequency> ret = new HashSet<Frequency>();
		int size = dataStream.readInt();
		
		try {
			for(int i = 0; i < size; i++)
			{
				Frequency freq = frequencyClass.newInstance();
				freq.read(dataStream);
				ret.add(freq);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return ret;
	}
	
	public String getName()
	{
		return owner != null ? owner + "FrequencyHandler" : "FrequencyHandler";
	}
	
	public static void reset()
	{
		for(FrequencyManager manager : managers)
		{
			manager.frequencies.clear();
			manager.dataHandler = null;
		}
		
		loaded = false;
	}
	
	public static class FrequencyDataHandler extends WorldSavedData
	{
		public FrequencyManager manager;
		
		public Set<Frequency> loadedFrequencies;
		
		public FrequencyDataHandler(String tagName)
		{
			super(tagName);
		}
		
		public void setManager(FrequencyManager m)
		{
			manager = m;
		}
		
		public void syncManager()
		{
			if(loadedFrequencies != null)
			{
				manager.frequencies = loadedFrequencies;
			}
		}
		
		@Override
		public void readFromNBT(NBTTagCompound nbtTags) 
		{
			try {
				String frequencyClass = nbtTags.getString("frequencyClass");
				
				if(nbtTags.hasKey("owner"))
				{
					manager.owner = nbtTags.getString("owner");
				}
				
				NBTTagList list = nbtTags.getTagList("invList", NBT.TAG_COMPOUND);
				
				loadedFrequencies = new HashSet<Frequency>();
				
				for(int i = 0; i < list.tagCount(); i++)
				{
					NBTTagCompound compound = list.getCompoundTagAt(i);
					
					Frequency freq = (Frequency)Class.forName(frequencyClass).newInstance();
					freq.read(compound);
					
					loadedFrequencies.add(freq);
				}
			} catch(Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public void writeToNBT(NBTTagCompound nbtTags) 
		{
			nbtTags.setString("frequencyClass", manager.frequencyClass.getName());
			
			if(manager.owner != null)
			{
				nbtTags.setString("owner", manager.owner);
			}
			
			NBTTagList list = new NBTTagList();
			
			for(Frequency freq : manager.getFrequencies())
			{
				NBTTagCompound compound = new NBTTagCompound();
				freq.write(compound);
				list.appendTag(compound);
			}
			
			nbtTags.setTag("freqList", list);
		}
	}
}
