package net.uberkat.obsidian.common;

import net.minecraft.src.*;

public class ItemObsidian extends Item 
{
	public ItemObsidian(int i)
	{
		super(i);
		itemsList[256 + i] = this;
		setCreativeTab(ObsidianIngots.tabOBSIDIAN);
	}

	public String getTextureFile() {
		return "/textures/items.png";
	}
}
