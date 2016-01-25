package mekanism.common.item;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ItemIngot extends ItemMekanism
{
	public static String[] en_USNames = {"Obsidian", "Osmium", "Bronze",
										"Glowstone", "Steel", "Copper", 
										"Tin"};

	public ItemIngot()
	{
		super();
		setHasSubtypes(true);
	}

/*
	@Override
	public void registerIcons(IIconRegister register)
	{
		for(int i = 0; i <= 6; i++)
		{
			icons[i] = register.registerIcon("mekanism:" + en_USNames[i] + "Ingot");
		}
	}

	@Override
	public IIcon getIconFromDamage(int meta)
	{
		return icons[meta];
	}
*/

	@Override
	public void getSubItems(Item item, CreativeTabs tabs, List<ItemStack> itemList)
	{
		for(int counter = 0; counter <= 6; counter++)
		{
			itemList.add(new ItemStack(item, 1, counter));
		}
	}

	@Override
	public String getUnlocalizedName(ItemStack item)
	{
		return "item." + en_USNames[item.getItemDamage()].toLowerCase() + "Ingot";
	}
}
