package mekanism.common;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

public class ItemDust extends ItemMekanism
{
	public static String[] en_USNames = {"Iron", "Gold", "Osmium", 
										"Obsidian", "Diamond", "Steel",
										"Copper", "Tin", "Silver"};
	
	public ItemDust(int id)
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
			case 0: return 248;
			case 1: return 250;
			case 2: return 242;
			case 3: return 241;
			case 4: return 249;
			case 5: return 245;
			case 6: return 243;
			case 7: return 244;
			case 8: return 246;
			default: return 0;
		}
	}

	@Override
	public void getSubItems(int id, CreativeTabs tabs, List itemList)
	{
		for (int counter = 0; counter <= 8; ++counter)
		{
			itemList.add(new ItemStack(this, 1, counter));
		}
	}

	@Override
	public String getItemNameIS(ItemStack item)
	{
		return "item." + en_USNames[item.getItemDamage()].toLowerCase() + "Dust";
	}
}
