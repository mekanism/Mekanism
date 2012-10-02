package net.uberkat.obsidian.common;

import net.minecraft.src.*;

public class ItemObsidian extends Item 
{
	public ItemObsidian(int i)
	{
		super(i);
		itemsList[256 + i] = this;
		setCreativeTab(CreativeTabs.tabAllSearch);
	}

	public String getTextureFile() {
		return "/obsidian/items.png";
	}
}
