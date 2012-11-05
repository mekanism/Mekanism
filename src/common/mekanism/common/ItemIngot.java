package mekanism.common;

import java.util.ArrayList;
import java.util.List;

import cpw.mods.fml.common.registry.LanguageRegistry;
import net.minecraft.src.*;

public class ItemIngot extends ItemMekanism
{
	public static String[] en_USNames = {"Obsidian", "Platinum", "Redstone",
										"Glowstone"};
	
	public ItemIngot(int id)
	{
		super(id);
		setHasSubtypes(true);
		setCreativeTab(Mekanism.tabMekanism);
	}

	public int getIconFromDamage(int meta)
	{
		switch (meta)
		{
			case 0: return 161;
			case 1: return 162;
			case 2: return 163;
			case 3: return 164;
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
		return "item." + en_USNames[item.getItemDamage()].toLowerCase() + "Ingot";
	}
}
