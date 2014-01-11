package mekanism.common.miner;

import java.util.ArrayList;
import java.util.List;

import mekanism.api.ListUtils;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import com.google.common.io.ByteArrayDataInput;

public class MItemStackFilter extends MinerFilter
{
	private static List<Integer> metaIgnoreArray = ListUtils.asList(Block.planks.blockID, Block.ladder.blockID, Block.torchWood.blockID,
			Block.furnaceBurning.blockID, Block.furnaceIdle.blockID, Block.dispenser.blockID, Block.pistonBase.blockID, 
			Block.pistonExtension.blockID, Block.pistonStickyBase.blockID, Block.pistonMoving.blockID);
	
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
		
		if(itemStack.itemID == itemType.itemID && metaIgnoreArray.contains(itemType.itemID))
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
		
		data.add(itemType.itemID);
		data.add(itemType.stackSize);
		data.add(itemType.getItemDamage());
	}
	
	@Override
	protected void read(ByteArrayDataInput dataStream)
	{
		itemType = new ItemStack(dataStream.readInt(), dataStream.readInt(), dataStream.readInt());
	}
	
	@Override
	public int hashCode() 
	{
		int code = 1;
		code = 31 * code + itemType.itemID;
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
