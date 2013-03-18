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
	public void func_94581_a(IconRegister register)
	{
		iconIndex = register.func_94245_a("mekanism:" + getUnlocalizedName().replace("item.", ""));
	}
}
