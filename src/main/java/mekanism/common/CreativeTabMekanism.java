package mekanism.common;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class CreativeTabMekanism extends CreativeTabs
{
	public CreativeTabMekanism()
	{
		super("tabMekanism");
	}

	@Nonnull
	@Override
	public ItemStack createIcon()
	{
		return new ItemStack(MekanismItems.AtomicAlloy);
	}
}
