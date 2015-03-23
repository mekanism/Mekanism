package mekanism.common.content.miner;

import java.util.ArrayList;

import mekanism.common.PacketHandler;
import mekanism.common.content.transporter.Finder.OreDictFinder;

import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import io.netty.buffer.ByteBuf;

public class MOreDictFilter extends MinerFilter
{
	public String oreDictName;

	@Override
	public boolean canFilter(ItemStack itemStack)
	{
		if(itemStack == null || !(itemStack.getItem() instanceof ItemBlock))
		{
			return false;
		}

		return new OreDictFinder(oreDictName).modifies(itemStack);
	}

	@Override
	public NBTTagCompound write(NBTTagCompound nbtTags)
	{
		super.write(nbtTags);
		
		nbtTags.setInteger("type", 1);
		nbtTags.setString("oreDictName", oreDictName);

		return nbtTags;
	}

	@Override
	protected void read(NBTTagCompound nbtTags)
	{
		super.read(nbtTags);
		
		oreDictName = nbtTags.getString("oreDictName");
	}

	@Override
	public void write(ArrayList data)
	{
		data.add(1);
		
		super.write(data);
		
		data.add(oreDictName);
	}

	@Override
	protected void read(ByteBuf dataStream)
	{
		super.read(dataStream);
		
		oreDictName = PacketHandler.readString(dataStream);
	}

	@Override
	public int hashCode()
	{
		int code = 1;
		code = 31 * code + oreDictName.hashCode();
		return code;
	}

	@Override
	public boolean equals(Object filter)
	{
		return super.equals(filter) && filter instanceof MOreDictFilter && ((MOreDictFilter)filter).oreDictName.equals(oreDictName);
	}

	@Override
	public MOreDictFilter clone()
	{
		MOreDictFilter filter = new MOreDictFilter();
		filter.replaceStack = replaceStack;
		filter.requireStack = requireStack;
		filter.oreDictName = oreDictName;

		return filter;
	}
}
