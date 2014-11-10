package mekanism.common.miner;

import java.util.ArrayList;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import io.netty.buffer.ByteBuf;

public abstract class MinerFilter
{
	public abstract boolean canFilter(ItemStack itemStack);

	public abstract NBTTagCompound write(NBTTagCompound nbtTags);

	protected abstract void read(NBTTagCompound nbtTags);

	public abstract void write(ArrayList data);

	protected abstract void read(ByteBuf dataStream);

	public static MinerFilter readFromNBT(NBTTagCompound nbtTags)
	{
		int type = nbtTags.getInteger("type");

		MinerFilter filter = null;

		if(type == 0)
		{
			filter = new MItemStackFilter();
		}
		else if(type == 1)
		{
			filter = new MOreDictFilter();
		}
		else if(type == 2)
		{
			filter = new MMaterialFilter();
		}
		else if(type == 3)
		{
			filter = new MModIDFilter();
		}

		filter.read(nbtTags);

		return filter;
	}

	public static MinerFilter readFromPacket(ByteBuf dataStream)
	{
		int type = dataStream.readInt();

		MinerFilter filter = null;

		if(type == 0)
		{
			filter = new MItemStackFilter();
		}
		else if(type == 1)
		{
			filter = new MOreDictFilter();
		}
		else if(type == 2)
		{
			filter = new MMaterialFilter();
		}
		else if(type == 3)
		{
			filter = new MModIDFilter();
		}

		filter.read(dataStream);

		return filter;
	}

	@Override
	public boolean equals(Object filter)
	{
		return filter instanceof MinerFilter;
	}
}
