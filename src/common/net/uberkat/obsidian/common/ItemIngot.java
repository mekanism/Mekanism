package net.uberkat.obsidian.common;

import java.util.ArrayList;
import java.util.List;

import cpw.mods.fml.common.registry.LanguageRegistry;
import net.minecraft.src.*;

public class ItemIngot extends ItemObsidian
{
	public static String[] en_USNames = {"Obsidian", "Platinum", "Redstone",
										"Glowstone", "Endium"};
	
	public ItemIngot(int id)
	{
		super(id);
		setHasSubtypes(true);
		setCreativeTab(ObsidianIngots.tabOBSIDIAN);
	}

	public int getIconFromDamage(int meta)
	{
		switch (meta)
		{
			case 0: return 161;
			case 1: return 162;
			case 2: return 163;
			case 3: return 164;
			case 4: return 174;
			default: return 0;
		}
	}

	public void getSubItems(int id, CreativeTabs tabs, List itemList)
	{
		for (int counter = 0; counter <= 4; ++counter)
		{
			itemList.add(new ItemStack(this, 1, counter));
		}
	}

	public String getItemNameIS(ItemStack item)
	{
		return en_USNames[item.getItemDamage()].toLowerCase() + "Ingot";
	}
}
