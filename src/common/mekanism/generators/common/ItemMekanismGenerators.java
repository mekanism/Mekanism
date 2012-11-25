package mekanism.generators.common;

import mekanism.common.ItemMekanism;
import net.minecraft.src.*;

public class ItemMekanismGenerators extends ItemMekanism
{
	public ItemMekanismGenerators(int id)
	{
		super(id);
	}
	
	@Override
	public String getTextureFile()
	{
		return "/resources/mekanism/textures/generators/items.png";
	}
}
