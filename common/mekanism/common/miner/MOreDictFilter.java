package mekanism.common.miner;

import java.util.ArrayList;

import mekanism.common.transporter.Finder.OreDictFinder;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import com.google.common.io.ByteArrayDataInput;

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
		nbtTags.setInteger("type", 1);
		nbtTags.setString("oreDictName", oreDictName);

		return nbtTags;
	}

	@Override
	protected void read(NBTTagCompound nbtTags)
	{
		oreDictName = nbtTags.getString("oreDictName");
	}

	@Override
	public void write(ArrayList data)
	{
		data.add(1);
		data.add(oreDictName);
	}

	@Override
	protected void read(ByteArrayDataInput dataStream)
	{
		oreDictName = dataStream.readUTF();
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
		filter.oreDictName = oreDictName;

		return filter;
	}
}
