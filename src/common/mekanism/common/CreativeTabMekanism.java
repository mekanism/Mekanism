package mekanism.common;

import net.minecraft.src.*;

public class CreativeTabMekanism extends CreativeTabs
{
	public CreativeTabMekanism()
	{
		super("tabMekanism");
	}
	
	@Override
	public ItemStack getIconItemStack()
	{
		return new ItemStack(Mekanism.AtomicCore);
	}
}
