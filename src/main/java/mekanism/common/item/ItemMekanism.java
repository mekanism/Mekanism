package mekanism.common.item;

import mekanism.common.Mekanism;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.Item;

public class ItemMekanism extends Item
{
	public ItemMekanism()
	{
		super();
		setCreativeTab(Mekanism.tabMekanism);
	}

	@Override
	public void registerIcons(IIconRegister register)
	{
		itemIcon = register.registerIcon("mekanism:" + getUnlocalizedName().replace("item.", ""));
	}
}
