package mekanism.common.content.miner;

import java.util.ArrayList;

import mekanism.common.util.MekanismUtils;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import io.netty.buffer.ByteBuf;

public abstract class MinerFilter
{
	public ItemStack replaceStack;
	
	public boolean requireStack;
	
	public abstract boolean canFilter(ItemStack itemStack);

	public NBTTagCompound write(NBTTagCompound nbtTags)
	{
		nbtTags.setBoolean("requireStack", requireStack);

		if(replaceStack != null)
		{
			nbtTags.setTag("replaceStack", replaceStack.writeToNBT(new NBTTagCompound()));
		}
		
		return nbtTags;
	}

	protected void read(NBTTagCompound nbtTags)
	{
		requireStack = nbtTags.getBoolean("requireStack");
		
		if(nbtTags.hasKey("replaceStack"))
		{
			replaceStack = ItemStack.loadItemStackFromNBT(nbtTags.getCompoundTag("replaceStack"));
		}
	}

	public void write(ArrayList data)
	{
		data.add(requireStack);
		
		if(replaceStack != null)
		{
			data.add(true);
			data.add(MekanismUtils.getID(replaceStack));
			data.add(replaceStack.getItemDamage());
		}
		else {
			data.add(false);
		}
	}

	protected void read(ByteBuf dataStream)
	{
		requireStack = dataStream.readBoolean();
		
		if(dataStream.readBoolean())
		{
			replaceStack = new ItemStack(Block.getBlockById(dataStream.readInt()), 1, dataStream.readInt());
		}
		else {
			replaceStack = null;
		}
	}

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
