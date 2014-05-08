package mekanism.common.transporter;

import java.util.ArrayList;

import mekanism.common.transporter.Finder.MaterialFinder;
import mekanism.common.transporter.Finder.OreDictFinder;
import mekanism.common.util.InventoryUtils;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

import com.google.common.io.ByteArrayDataInput;

public class TMaterialFilter extends TransporterFilter
{
	public ItemStack materialItem;
	
	public Material getMaterial()
	{
		return Blocks.blocksList[materialItem.itemID].getMaterial();
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

		data.add(materialItem.itemID);
		data.add(materialItem.stackSize);
		data.add(materialItem.getItemDamage());
	}

	@Override
	protected void read(ByteArrayDataInput dataStream)
	{
		super.read(dataStream);
		
		materialItem = new ItemStack(dataStream.readInt(), dataStream.readInt(), dataStream.readInt());
	}

	@Override
	public int hashCode()
	{
		int code = 1;
		code = 31 * code + materialItem.itemID;
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
