package mekanism.common;

import java.util.HashMap;
import java.util.Map;

import mekanism.api.EnumColor;
import mekanism.common.util.MekanismUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants.NBT;

public enum Upgrade
{
	SPEED("speed", 8, EnumColor.RED),
	ENERGY("energy", 8, EnumColor.BRIGHT_GREEN);
	
	private String name;
	private int maxStack;
	private EnumColor color;
	private boolean canMultiply;
	
	private Upgrade(String s, int max, EnumColor c)
	{
		name = s;
		maxStack = max;
		color = c;
	}
	
	public String getName()
	{
		return MekanismUtils.localize("upgrade." + name);
	}
	
	public String getDescription()
	{
		return MekanismUtils.localize("upgrade." + name + ".desc");
	}
	
	public int getMax()
	{
		return maxStack;
	}
	
	public EnumColor getColor()
	{
		return color;
	} 
	
	public boolean canMultiply()
	{
		return getMax() > 1;
	}
	
	public ItemStack getStack()
	{
		switch(this)
		{
			case SPEED:
				return new ItemStack(Mekanism.SpeedUpgrade);
			case ENERGY:
				return new ItemStack(Mekanism.EnergyUpgrade);
		}
		
		return null;
	}
	
	public static Map<Upgrade, Integer> buildMap(NBTTagCompound nbtTags)
	{
		Map<Upgrade, Integer> upgrades = new HashMap<Upgrade, Integer>();
		
		if(nbtTags != null && nbtTags.hasKey("upgrades"))
		{
			NBTTagList list = nbtTags.getTagList("upgrades", NBT.TAG_COMPOUND);
			
			for(int tagCount = 0; tagCount < list.tagCount(); tagCount++)
			{
				NBTTagCompound compound = (NBTTagCompound)list.getCompoundTagAt(tagCount);
				
				Upgrade upgrade = Upgrade.values()[compound.getInteger("type")];
				upgrades.put(upgrade, compound.getInteger("amount"));
			}
		}
		
		return upgrades;
	}
	
	public static void saveMap(Map<Upgrade, Integer> upgrades, NBTTagCompound nbtTags)
	{
		NBTTagList list = new NBTTagList();
		
		for(Map.Entry<Upgrade, Integer> entry : upgrades.entrySet())
		{
			list.appendTag(getTagFor(entry.getKey(), entry.getValue()));
		}
		
		nbtTags.setTag("upgrades", list);
	}
	
	public static NBTTagCompound getTagFor(Upgrade upgrade, int amount)
	{
		NBTTagCompound compound = new NBTTagCompound();
		
		compound.setInteger("type", upgrade.ordinal());
		compound.setInteger("amount", amount);
		
		return compound;
	}
}