package mekanism.common.base;

import java.util.ArrayList;

import net.minecraft.nbt.NBTTagCompound;

import io.netty.buffer.ByteBuf;

public interface ITileComponent
{
	public void tick();

	public void read(NBTTagCompound nbtTags);

	public void read(ByteBuf dataStream);

	public void write(NBTTagCompound nbtTags);

	public void write(ArrayList data);
}
