package mekanism.common.miner;

import java.util.ArrayList;
import java.util.List;

import mekanism.api.ListUtils;
import mekanism.common.util.MekanismUtils;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import io.netty.buffer.ByteBuf;

public class MItemStackFilter extends MinerFilter
{
	private static List<Block> metaIgnoreArray = ListUtils.asList(Blocks.planks, Blocks.ladder, Blocks.torch,
			Blocks.furnace, Blocks.dispenser, Blocks.piston,
			Blocks.piston_extension, Blocks.piston_head);

	public ItemStack itemType;

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

		if(itemStack.getItem() == itemType.getItem() && metaIgnoreArray.contains(Block.getBlockFromItem(itemType.getItem())))
		{
			return true;
		}

		return itemType.isItemEqual(itemStack);
	}

	@Override
	public NBTTagCompound write(NBTTagCompound nbtTags)
	{
		nbtTags.setInteger("type", 0);
		itemType.writeToNBT(nbtTags);

		return nbtTags;
	}

	@Override
	protected void read(NBTTagCompound nbtTags)
	{
		itemType = ItemStack.loadItemStackFromNBT(nbtTags);
	}

	@Override
	public void write(ArrayList data)
	{
		data.add(0);

		data.add(MekanismUtils.getID(itemType));
		data.add(itemType.stackSize);
		data.add(itemType.getItemDamage());
	}

	@Override
	protected void read(ByteBuf dataStream)
	{
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
		filter.itemType = itemType.copy();

		return filter;
	}
}
