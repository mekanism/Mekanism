package mekanism.common.content.transporter;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import mekanism.api.EnumColor;
import mekanism.common.util.TransporterUtils;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

public abstract class TransporterFilter
{
	public static final int MAX_LENGTH = 24;
	
	public static final List<Character> SPECIAL_CHARS = Arrays.asList('*', '-', ' ', '|');
	
	public EnumColor color;

	public abstract boolean canFilter(ItemStack itemStack);

	public abstract InvStack getStackFromInventory(IInventory inv, ForgeDirection side);

	public void write(NBTTagCompound nbtTags)
	{
		if(color != null)
		{
			nbtTags.setInteger("color", TransporterUtils.colors.indexOf(color));
		}
	}

	protected void read(NBTTagCompound nbtTags)
	{
		if(nbtTags.hasKey("color"))
		{
			color = TransporterUtils.colors.get(nbtTags.getInteger("color"));
		}
	}

	public void write(ArrayList data)
	{
		if(color != null)
		{
			data.add(TransporterUtils.colors.indexOf(color));
		}
		else {
			data.add(-1);
		}
	}

	protected void read(ByteBuf dataStream)
	{
		int c = dataStream.readInt();

		if(c != -1)
		{
			color = TransporterUtils.colors.get(c);
		}
		else {
			color = null;
		}
	}

	public static TransporterFilter readFromNBT(NBTTagCompound nbtTags)
	{
		int type = nbtTags.getInteger("type");

		TransporterFilter filter = null;

		if(type == 0)
		{
			filter = new TItemStackFilter();
		}
		else if(type == 1)
		{
			filter = new TOreDictFilter();
		}
		else if(type == 2)
		{
			filter = new TMaterialFilter();
		}
		else if(type == 3)
		{
			filter = new TModIDFilter();
		}

		filter.read(nbtTags);

		return filter;
	}

	public static TransporterFilter readFromPacket(ByteBuf dataStream)
	{
		int type = dataStream.readInt();

		TransporterFilter filter = null;

		if(type == 0)
		{
			filter = new TItemStackFilter();
		}
		else if(type == 1)
		{
			filter = new TOreDictFilter();
		}
		else if(type == 2)
		{
			filter = new TMaterialFilter();
		}
		else if(type == 3)
		{
			filter = new TModIDFilter();
		}

		filter.read(dataStream);

		return filter;
	}

	@Override
	public int hashCode()
	{
		int code = 1;
		code = 31 * code + (color != null ? color.ordinal() : -1);
		return code;
	}

	@Override
	public boolean equals(Object filter)
	{
		return filter instanceof TransporterFilter && ((TransporterFilter)filter).color == color;
	}
}
