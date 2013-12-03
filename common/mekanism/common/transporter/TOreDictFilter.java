package mekanism.common.transporter;

import java.util.ArrayList;

import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ForgeDirection;

import com.google.common.io.ByteArrayDataInput;

public class TOreDictFilter extends TransporterFilter
{
	public String oreDictName;
	
	@Override
	public boolean canFilter(ItemStack itemStack)
	{
		String oreKey = MekanismUtils.getOreDictName(itemStack);
		
		if(oreKey == null)
		{
			return false;
		}
		
		if(oreDictName.equals(oreKey) || oreDictName.equals("*"))
		{
			return true;
		}
		else if(oreDictName.endsWith("*") && !oreDictName.startsWith("*"))
		{
			if(oreKey.startsWith(oreDictName.substring(0, oreDictName.length()-1)))
			{
				return true;
			}
		}
		else if(oreDictName.startsWith("*") && !oreDictName.endsWith("*"))
		{
			if(oreKey.endsWith(oreDictName.substring(1)))
			{
				return true;
			}
		}
		else if(oreDictName.startsWith("*") && oreDictName.endsWith("*"))
		{
			if(oreKey.contains(oreDictName.substring(1, oreDictName.length()-1)))
			{
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public InvStack getStackFromInventory(IInventory inv, ForgeDirection side)
	{
		return InventoryUtils.takeTopStack(inv, side.ordinal());
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
	protected void read(ByteArrayDataInput dataStream)
	{
		super.read(dataStream);
		
		oreDictName = dataStream.readUTF();
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
