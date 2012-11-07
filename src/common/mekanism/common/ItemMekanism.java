package mekanism.common;

import net.minecraft.src.*;

public class ItemMekanism extends Item 
{
	public ItemMekanism(int i)
	{
		super(i);
		setCreativeTab(Mekanism.tabMekanism);
	}

	@Override
	public String getTextureFile() 
	{
		return "/resources/mekanism/textures/items.png";
	}
}
