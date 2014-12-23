package mekanism.common.item;

import java.util.List;

import mekanism.common.Mekanism;

import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public class ItemIngot extends ItemMekanism
{
	public TextureAtlasSprite[] icons = new TextureAtlasSprite[256];

	public static String[] en_USNames = {"Obsidian", "Osmium", "Bronze", "Glowstone", "Steel", "Copper", "Tin"};

	public ItemIngot()
	{
		super();
		setHasSubtypes(true);
		setCreativeTab(Mekanism.tabMekanism);
	}

/*
	@Override
	public void registerIcons(TextureMap register)
	{
		for(int i = 0; i <= 6; i++)
		{
			icons[i] = register.registerIcon("mekanism:" + en_USNames[i] + "Ingot");
		}
	}

	@Override
	public TextureAtlasSprite getIconFromDamage(int meta)
	{
		return icons[meta];
	}
*/

	@Override
	public void getSubItems(Item item, CreativeTabs tabs, List itemList)
	{
		for(int counter = 0; counter <= 6; ++counter)
		{
			itemList.add(new ItemStack(item, 1, counter));
		}
	}

	@Override
	public String getUnlocalizedName(ItemStack item)
	{
		return "item." + en_USNames[item.getItemDamage()].toLowerCase() + "Ingot";
	}
}
