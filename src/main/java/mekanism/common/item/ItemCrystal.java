package mekanism.common.item;

import java.util.List;

import mekanism.common.Resource;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

public class ItemCrystal extends ItemMekanism
{
	public IIcon[] icons = new IIcon[256];

	public ItemCrystal()
	{
		super();
		setHasSubtypes(true);
	}

	@Override
	public void registerIcons(IIconRegister register)
	{
		for(int i = 0; i < Resource.values().length; i++)
		{
			icons[i] = register.registerIcon("mekanism:" + Resource.values()[i].getName() + "Crystal");
		}
	}

	@Override
	public IIcon getIconFromDamage(int meta)
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
		if(item.getItemDamage() <= Resource.values().length-1)
		{
			return "item." + Resource.values()[item.getItemDamage()].getName().toLowerCase() + "Crystal";
		}
		
		return "Invalid";
	}
}
