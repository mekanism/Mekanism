package mekanism.common.content.miner;

import java.util.ArrayList;

import mekanism.common.util.MekanismUtils;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import io.netty.buffer.ByteBuf;

public class MItemStackFilter extends MinerFilter
{
	public ItemStack itemType;
	public boolean fuzzy;

	public MItemStackFilter(ItemStack item)
	{
		itemType = item;
	}

	public MItemStackFilter() {}

	@Override
	public boolean canFilter(ItemStack itemStack)
	{
		if(itemStack == null)
		{
			return false;
		}

		if(itemStack.getItem() == itemType.getItem() && fuzzy)
		{
			return true;
		}

		return itemType.isItemEqual(itemStack);
	}

	@Override
	public NBTTagCompound write(NBTTagCompound nbtTags)
	{
		super.write(nbtTags);
		
		nbtTags.setInteger("type", 0);
		
		nbtTags.setBoolean("fuzzy", fuzzy);
		itemType.writeToNBT(nbtTags);

		return nbtTags;
	}

	@Override
	protected void read(NBTTagCompound nbtTags)
	{
		super.read(nbtTags);
		
		fuzzy = nbtTags.getBoolean("fuzzy");
		itemType = ItemStack.loadItemStackFromNBT(nbtTags);
	}

	@Override
	public void write(ArrayList data)
	{
		data.add(0);
		
		super.write(data);

		data.add(fuzzy);
		
		data.add(MekanismUtils.getID(itemType));
		data.add(itemType.stackSize);
		data.add(itemType.getItemDamage());
	}

	@Override
	protected void read(ByteBuf dataStream)
	{
		super.read(dataStream);
		
		fuzzy = dataStream.readBoolean();
		
		itemType = new ItemStack(Item.getItemById(dataStream.readInt()), dataStream.readInt(), dataStream.readInt());
	}

	@Override
	public int hashCode()
	{
		int code = 1;
		code = 31 * code + MekanismUtils.getID(itemType);
		code = 31 * code + itemType.stackSize;
		code = 31 * code + itemType.getItemDamage();
		return code;
	}

	@Override
	public boolean equals(Object filter)
	{
		return super.equals(filter) && filter instanceof MItemStackFilter && ((MItemStackFilter)filter).itemType.isItemEqual(itemType);
	}

	@Override
	public MItemStackFilter clone()
	{
		MItemStackFilter filter = new MItemStackFilter();
		filter.replaceStack = replaceStack;
		filter.requireStack = requireStack;
		filter.fuzzy = fuzzy;
		filter.itemType = itemType.copy();

		return filter;
	}
}
