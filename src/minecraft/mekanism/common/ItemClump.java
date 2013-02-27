package mekanism.common;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

public class ItemClump extends ItemMekanism
{
	public static String[] en_USNames = {"Iron", "Gold", "Osmium", 
										"Copper", "Tin", "Silver"};
	
	public ItemClump(int id)
	{
		super(id);
		setHasSubtypes(true);
		setCreativeTab(Mekanism.tabMekanism);
	}

	@Override
	public int getIconFromDamage(int meta)
	{
		switch (meta)
		{
			case 0: return 216;
			case 1: return 218;
			case 2: return 210;
			case 3: return 211;
			case 4: return 212;
			case 5: return 214;
			default: return 0;
		}
	}

	@Override
	public void getSubItems(int id, CreativeTabs tabs, List itemList)
	{
		for (int counter = 0; counter <= 5; ++counter)
		{
			itemList.add(new ItemStack(this, 1, counter));
		}
	}

	@Override
	public String getItemNameIS(ItemStack item)
	{
		return "item." + en_USNames[item.getItemDamage()].toLowerCase() + "Clump";
	}
}
