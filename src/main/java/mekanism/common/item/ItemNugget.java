package mekanism.common.item;

import mekanism.common.base.IMetaItem;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import java.util.Locale;

public class ItemNugget extends ItemMekanism implements IMetaItem
{
	public ItemNugget()
	{
		super();
		setHasSubtypes(true);
	}

	@Override
	public String getTexture(int meta)
	{
		return ItemIngot.en_USNames[meta] + "Nugget";
	}

	@Override
	public int getVariants()
	{
		return ItemIngot.en_USNames.length;
	}

	@Override
	public void getSubItems(CreativeTabs tabs, NonNullList<ItemStack> itemList)
	{
		if(!isInCreativeTab(tabs)) return;
		for(int counter = 0; counter < ItemIngot.en_USNames.length; counter++)
		{
			itemList.add(new ItemStack(this, 1, counter));
		}
	}

	@Override
	public String getUnlocalizedName(ItemStack item)
	{
		return "item." + ItemIngot.en_USNames[item.getItemDamage()].toLowerCase(Locale.ROOT) + "Nugget";
	}
}
