package mekanism.common.item;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ItemOtherDust extends ItemMekanism
{
	public static String[] subtypes = {"Diamond", "Steel", "Lead",
									  "Sulfur", "Lithium", "RefinedObsidian",
									  "Obsidian"};
	
	public ItemOtherDust()
	{
		super();
		setHasSubtypes(true);
	}
	
/*
	@Override
	public void registerIcons(IIconRegister register)
	{
		for(int i = 0; i < subtypes.length; i++)
		{
			icons[i] = register.registerIcon("mekanism:" + subtypes[i] + "Dust");
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
		for(int counter = 0; counter < subtypes.length; counter++)
		{
			itemList.add(new ItemStack(this, 1, counter));
		}
	}

	@Override
	public String getUnlocalizedName(ItemStack item)
	{
		return "item." + subtypes[item.getItemDamage()].toLowerCase() + "Dust";
	}
}
