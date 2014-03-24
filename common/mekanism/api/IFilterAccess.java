package mekanism.api;

import net.minecraft.nbt.NBTTagCompound;

public interface IFilterAccess
{
	public NBTTagCompound getFilterData(NBTTagCompound nbtTags);
	
	public void setFilterData(NBTTagCompound nbtTags);
	
	public String getDataType();
}
