package mekanism.common;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class CreativeTabMekanism extends CreativeTabs
{
	public CreativeTabMekanism()
	{
		super("tabMekanism");
	}

	@Override
	public ItemStack getIconItemStack()
	{
		return new ItemStack(Mekanism.AtomicAlloy);
	}

	@Override
	public Item getTabIconItem() 
	{
		return Mekanism.AtomicAlloy;
	}
}
