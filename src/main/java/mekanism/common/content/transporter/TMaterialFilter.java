package mekanism.common.content.transporter;

import java.util.ArrayList;

import mekanism.common.content.transporter.Finder.MaterialFinder;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

import io.netty.buffer.ByteBuf;

public class TMaterialFilter extends TransporterFilter
{
	public ItemStack materialItem;
	
	public Material getMaterial()
	{
		return Block.getBlockFromItem(materialItem.getItem()).getMaterial();
	}

	@Override
	public boolean canFilter(ItemStack itemStack)
	{
		if(itemStack == null || !(itemStack.getItem() instanceof ItemBlock))
		{
			return false;
		}

		return new MaterialFinder(getMaterial()).modifies(itemStack);
	}
	
	@Override
	public InvStack getStackFromInventory(IInventory inv, ForgeDirection side)
	{
		return InventoryUtils.takeTopStack(inv, side.ordinal(), new MaterialFinder(getMaterial()));
	}

	@Override
	public void write(NBTTagCompound nbtTags)
	{
		super.write(nbtTags);
		
		nbtTags.setInteger("type", 2);
		materialItem.writeToNBT(nbtTags);
	}

	@Override
	protected void read(NBTTagCompound nbtTags)
	{
		super.read(nbtTags);
		
		materialItem = ItemStack.loadItemStackFromNBT(nbtTags);
	}

	@Override
	public void write(ArrayList data)
	{
		data.add(2);
		
		super.write(data);

		data.add(MekanismUtils.getID(materialItem));
		data.add(materialItem.stackSize);
		data.add(materialItem.getItemDamage());
	}

	@Override
	protected void read(ByteBuf dataStream)
	{
		super.read(dataStream);
		
		materialItem = new ItemStack(Item.getItemById(dataStream.readInt()), dataStream.readInt(), dataStream.readInt());
	}

	@Override
	public int hashCode()
	{
		int code = 1;
		code = 31 * code + MekanismUtils.getID(materialItem);
		code = 31 * code + materialItem.stackSize;
		code = 31 * code + materialItem.getItemDamage();
		return code;
	}

	@Override
	public boolean equals(Object filter)
	{
		return super.equals(filter) && filter instanceof TMaterialFilter && ((TMaterialFilter)filter).materialItem.isItemEqual(materialItem);
	}

	@Override
	public TMaterialFilter clone()
	{
		TMaterialFilter filter = new TMaterialFilter();
		filter.materialItem = materialItem;

		return filter;
	}
}
