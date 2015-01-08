package mekanism.common.multiblock;

import java.util.HashSet;

import mekanism.api.Coord4D;

import net.minecraft.nbt.NBTTagCompound;

public abstract class MultiblockCache<T extends SynchronizedData<T>>
{
	public HashSet<Coord4D> locations = new HashSet<Coord4D>();
	
	public abstract void apply(T data);
	
	public abstract void sync(T data);
	
	public abstract void load(NBTTagCompound nbtTags);
	
	public abstract void save(NBTTagCompound nbtTags);
}
