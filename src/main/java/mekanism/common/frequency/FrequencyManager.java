package mekanism.common.frequency;

import io.netty.buffer.ByteBuf;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import mekanism.api.Coord4D;
import mekanism.common.tile.TileEntityTeleporter;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
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
				dataHandler.markDirty();
				
				return iterFreq;
			}
		}
		
		deactivate(coord);
		
		return null;
	}
	
	public void remove(String name, String owner)
	{
		for(Iterator<Frequency> iter = getFrequencies().iterator(); iter.hasNext();)
		{
			Frequency iterFreq = iter.next();
			
			if(iterFreq.name.equals(name) && iterFreq.owner.equals(owner))
			{
				iter.remove();
				dataHandler.markDirty();
			}
		}
	}
	
	public void remove(String name)
	{
		for(Iterator<Frequency> iter = getFrequencies().iterator(); iter.hasNext();)
		{
			Frequency iterFreq = iter.next();
			
			if(iterFreq.name.equals(name))
			{
				iter.remove();
				dataHandler.markDirty();
			}
		}
	}
	
	public int removeAll(String user)
	{
		int amount = 0;
		
		for(Iterator<Frequency> iter = getFrequencies().iterator(); iter.hasNext();)
		{
			Frequency iterFreq = iter.next();
			
			if(iterFreq.owner.equals(user))
			{
				iter.remove();
				dataHandler.markDirty();
				amount++;
			}
		}
		
		return amount;
	}
	
	public void deactivate(Coord4D coord)
	{
		for(Frequency freq : frequencies)
		{
			freq.activeCoords.remove(coord);
			dataHandler.markDirty();
		}
	}
	
	public Frequency validateFrequency(String user, Coord4D coord, Frequency freq)
	{
		for(Frequency iterFreq : frequencies)
		{
			if(freq.equals(iterFreq))
			{
				iterFreq.activeCoords.add(coord);
				dataHandler.markDirty();
				
				return iterFreq;
			}
		}
		
		if(user.equals(freq.owner))
		{
			freq.activeCoords.add(coord);
			frequencies.add(freq);
			dataHandler.markDirty();
			
			return freq;
		}
		
		return null;
	}
	
	public void createOrLoad(World world)
	{
		String name = getName();
		
		if(dataHandler == null)
		{
			dataHandler = (FrequencyDataHandler)world.perWorldStorage.loadData(FrequencyDataHandler.class, name);
			
			if(dataHandler == null)
			{
				dataHandler = new FrequencyDataHandler(name);
				dataHandler.setManager(this);
				world.perWorldStorage.setData(name, dataHandler);
			}
			else {
				dataHandler.setManager(this);
				dataHandler.syncManager();
			}
		}
	}
	
	public static FrequencyManager loadOnly(World world, String owner, Class<? extends Frequency> freqClass)
	{
		FrequencyManager manager = new FrequencyManager(freqClass);
		String name = manager.getName();
		
		FrequencyDataHandler handler = (FrequencyDataHandler)world.perWorldStorage.loadData(FrequencyDataHandler.class, name);
		
		if(handler == null)
		{
			return null;
		}
		else {
			manager.dataHandler = handler;
			manager.dataHandler.syncManager();
			
			return manager;
		}
	}
	
	public Set<Frequency> getFrequencies()
	{
		return frequencies;
	}
	
	public void addFrequency(Frequency freq)
	{
		frequencies.add(freq);
		dataHandler.markDirty();
	}
	
	public boolean containsFrequency(String name)
	{
		for(Frequency freq : frequencies)
		{
			if(freq.name.equals(name))
			{
				return true;
			}
		}
		
		return false;
	}
	
	public static void tick(World world)
	{
		if(!loaded)
		{
			load(world);
		}
		
		for(FrequencyManager manager : managers)
		{
			manager.tickSelf(world);
		}
	}

	public void tickSelf(World world)
	{
		for(Frequency iterFreq : frequencies)
		{
			for(Iterator<Coord4D> iter = iterFreq.activeCoords.iterator(); iter.hasNext();)
			{
				Coord4D coord = iter.next();
				
				if(coord.dimensionId == world.provider.dimensionId)
				{
					if(!coord.exists(world))
					{
						iter.remove();
					}
					else {
						TileEntity tile = coord.getTileEntity(world);
						
						if(!(tile instanceof TileEntityTeleporter))
						{
							iter.remove();
						}
						else {
							Frequency freq = ((TileEntityTeleporter)tile).frequency;
							
							if(freq == null || !freq.equals(iterFreq))
							{
								iter.remove();
							}
						}
					}
				}
			}
		}
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
		public String loadedOwner;
		
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
				manager.owner = loadedOwner;
			}
		}
		
		@Override
		public void readFromNBT(NBTTagCompound nbtTags) 
		{
			try {
				String frequencyClass = nbtTags.getString("frequencyClass");
				
				if(nbtTags.hasKey("owner"))
				{
					loadedOwner = nbtTags.getString("owner");
				}
				
				NBTTagList list = nbtTags.getTagList("freqList", NBT.TAG_COMPOUND);
				
				loadedFrequencies = new HashSet<Frequency>();
				
				for(int i = 0; i < list.tagCount(); i++)
				{
					NBTTagCompound compound = list.getCompoundTagAt(i);
					
					Constructor c = Class.forName(frequencyClass).getConstructor(new Class[] {NBTTagCompound.class});
					Frequency freq = (Frequency)c.newInstance(compound);
					
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
