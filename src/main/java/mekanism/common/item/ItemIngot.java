package mekanism.common.item;

import mekanism.common.base.IMetaItem;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public class ItemIngot extends ItemMekanism implements IMetaItem
{
	public static String[] en_USNames = {"Obsidian", "Osmium", "Bronze",
										"Glowstone", "Steel", "Copper", 
										"Tin"};

	public ItemIngot()
	{
		super();
		setHasSubtypes(true);
	}
	
	@Override
	public String getTexture(int meta)
	{
		return en_USNames[meta] + "Ingot";
	}
	
	@Override
	public int getVariants()
	{
		return en_USNames.length;
	}

	@Override
	public void getSubItems(CreativeTabs tabs, NonNullList<ItemStack> itemList)
	{
		if(!isInCreativeTab(tabs)) return;
		for(int counter = 0; counter < en_USNames.length; counter++)
		{
			itemList.add(new ItemStack(this, 1, counter));
		}
	}

	@Override
	public String getUnlocalizedName(ItemStack item)
	{
		return "item." + en_USNames[item.getItemDamage()].toLowerCase() + "Ingot";
	}
}
