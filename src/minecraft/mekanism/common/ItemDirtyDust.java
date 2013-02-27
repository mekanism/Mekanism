package mekanism.common;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

public class ItemDirtyDust extends ItemMekanism
{
	public static String[] en_USNames = {"Iron", "Gold", "Osmium", 
										"Copper", "Tin", "Silver",
										"Obsidian"};
	
	public ItemDirtyDust(int id)
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
			case 0: return 200;
			case 1: return 202;
			case 2: return 194;
			case 3: return 195;
			case 4: return 196;
			case 5: return 198;
			case 6: return 193;
			default: return 0;
		}
	}

	@Override
	public void getSubItems(int id, CreativeTabs tabs, List itemList)
	{
		for (int counter = 0; counter <= 6; ++counter)
		{
			itemList.add(new ItemStack(this, 1, counter));
		}
	}

	@Override
	public String getItemNameIS(ItemStack item)
	{
		return "item.dirty" + en_USNames[item.getItemDamage()] + "Dust";
	}
}
