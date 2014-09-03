package mekanism.common.content.transporter;

import java.util.ArrayList;

import mekanism.common.PacketHandler;
import mekanism.common.content.transporter.Finder.OreDictFinder;
import mekanism.common.util.InventoryUtils;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

import io.netty.buffer.ByteBuf;

public class TOreDictFilter extends TransporterFilter
{
	public String oreDictName;

	@Override
	public boolean canFilter(ItemStack itemStack)
	{
		if(itemStack == null)
		{
			return false;
		}

		return new OreDictFinder(oreDictName).modifies(itemStack);
	}

	@Override
	public InvStack getStackFromInventory(IInventory inv, ForgeDirection side)
	{
		return InventoryUtils.takeTopStack(inv, side.ordinal(), new OreDictFinder(oreDictName));
	}

	@Override
	public void write(NBTTagCompound nbtTags)
	{
		super.write(nbtTags);

		nbtTags.setInteger("type", 1);
		nbtTags.setString("oreDictName", oreDictName);
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
		code = 31 * code + super.hashCode();
		code = 31 * code + oreDictName.hashCode();
		return code;
	}

	@Override
	public boolean equals(Object filter)
	{
		return super.equals(filter) && filter instanceof TOreDictFilter && ((TOreDictFilter)filter).oreDictName.equals(oreDictName);
	}

	@Override
	public TOreDictFilter clone()
	{
		TOreDictFilter filter = new TOreDictFilter();
		filter.color = color;
		filter.oreDictName = oreDictName;

		return filter;
	}
}
