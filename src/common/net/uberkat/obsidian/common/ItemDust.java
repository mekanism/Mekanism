package net.uberkat.obsidian.common;

import java.util.ArrayList;
import java.util.List;

import cpw.mods.fml.common.registry.LanguageRegistry;
import net.minecraft.src.*;

public class ItemDust extends ItemObsidian
{
	public static String[] en_USNames = {"Iron", "Gold", "Platinum", 
										"Obsidian"};
	
	public ItemDust(int id)
	{
		super(id);
		setHasSubtypes(true);
		setCreativeTab(ObsidianIngots.tabOBSIDIAN);
	}

	public int getIconFromDamage(int meta)
	{
		switch (meta)
		{
			case 0: return 248;
			case 1: return 250;
			case 2: return 242;
			case 3: return 241;
			default: return 0;
		}
	}

	public void getSubItems(int id, CreativeTabs tabs, List itemList)
	{
		for (int counter = 0; counter <= 3; ++counter)
		{
			itemList.add(new ItemStack(this, 1, counter));
		}
	}

	public String getItemNameIS(ItemStack item)
	{
		return "item." + en_USNames[item.getItemDamage()].toLowerCase() + "Dust";
	}
}
