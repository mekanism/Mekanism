package net.uberkat.obsidian.common;

import net.minecraft.src.*;

public class CreativeTabOI extends CreativeTabs
{
	public CreativeTabOI()
	{
		super("tabObsidian");
	}
	
	public ItemStack getIconItemStack()
	{
		return new ItemStack(ObsidianIngots.ObsidianIngot);
	}
}
