package mekanism.common;

import java.util.ArrayList;

import com.google.common.io.ByteArrayDataInput;

import net.minecraft.nbt.NBTTagCompound;

public interface ITileComponent 
{
	public void tick();
	
	public void read(NBTTagCompound nbtTags);
	
	public void read(ByteArrayDataInput dataStream);
	
	public void write(NBTTagCompound nbtTags);
	
	public void write(ArrayList data);
}
