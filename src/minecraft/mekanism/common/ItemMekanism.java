package mekanism.common;

import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.item.Item;
import net.minecraft.util.Icon;

public class ItemMekanism extends Item 
{
	public ItemMekanism(int i)
	{
		super(i);
		setCreativeTab(Mekanism.tabMekanism);
	}
	
	@Override
	public void updateIcons(IconRegister register)
	{
		iconIndex = register.registerIcon("mekanism:" + getUnlocalizedName().replace("item.", ""));
	}
}
