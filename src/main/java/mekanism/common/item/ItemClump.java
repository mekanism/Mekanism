package mekanism.common.item;

import java.util.List;

import mekanism.common.Mekanism;
import mekanism.common.Resource;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

public class ItemClump extends ItemMekanism
{
	public IIcon[] icons = new IIcon[256];

	public ItemClump(int id)
	{
		super(id);
		setHasSubtypes(true);
		setCreativeTab(Mekanism.tabMekanism);
	}

	@Override
	public void registerIcons(IIconRegister register)
	{
		for(int i = 0; i < Resource.values().length; i++)
		{
			icons[i] = register.registerIcon("mekanism:" + Resource.values()[i].getName() + "Clump");
		}
	}

	@Override
	public IIcon getIconFromDamage(int meta)
	{
		return icons[meta];
	}

	@Override
	public void getSubItems(int id, CreativeTabs tabs, List itemList)
	{
		for(int counter = 0; counter < Resource.values().length; counter++)
		{
			itemList.add(new ItemStack(this, 1, counter));
		}
	}

	@Override
	public String getUnlocalizedName(ItemStack item)
	{
		return "item." + Resource.values()[item.getItemDamage()].getName().toLowerCase() + "Clump";
	}
}
