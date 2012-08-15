package net.uberkat.obsidian.common;

import net.minecraft.src.*;

public class ItemBlockBase extends ItemBlock
{
	public ItemBlockBase(int i, Block block)
	{
		super(i);
		setMaxDamage(0);
		setHasSubtypes(true);
	}
	
	@Override
	public String getItemNameIS(ItemStack itemstack)
	{
		String name = "";
		switch(itemstack.getItemDamage())
		{
			case 0:
				name = "baseObsidian";
				break;
			case 1:
				name = "baseRedstone";
				break;
			case 2:
				name = "basePlatinum";
				break;
			case 3:
				name = "basePlatinumOre";
				break;
			case 4:
				name = "baseCoal";
				break;
			case 5:
				name = "baseGlowstone";
				break;
		}
		return name;
	}
	
	public int getMetadata(int i)
	{
		return i;
	}
}
