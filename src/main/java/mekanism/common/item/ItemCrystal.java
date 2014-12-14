package mekanism.common.item;

import java.util.List;

import mekanism.common.Mekanism;
import mekanism.common.Resource;

import net.minecraft.client.renderer.texture.TextureAtlasSpriteRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public class ItemCrystal extends ItemMekanism
{
	public TextureAtlasSprite[] icons = new TextureAtlasSprite[256];

	public ItemCrystal()
	{
		super();
		setHasSubtypes(true);
		setCreativeTab(Mekanism.tabMekanism);
	}

	@Override
	public void registerIcons(TextureAtlasSpriteRegister register)
	{
		for(int i = 0; i < Resource.values().length; i++)
		{
			icons[i] = register.registerIcon("mekanism:" + Resource.values()[i].getName() + "Crystal");
		}
	}

	@Override
	public TextureAtlasSprite getIconFromDamage(int meta)
	{
		return icons[meta];
	}

	@Override
	public void getSubItems(Item item, CreativeTabs tabs, List itemList)
	{
		for(int counter = 0; counter < Resource.values().length; counter++)
		{
			itemList.add(new ItemStack(item, 1, counter));
		}
	}

	@Override
	public String getUnlocalizedName(ItemStack item)
	{
		return "item." + Resource.values()[item.getItemDamage()].getName().toLowerCase() + "Crystal";
	}
}
