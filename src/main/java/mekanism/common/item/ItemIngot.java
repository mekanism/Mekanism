package mekanism.common.item;

import java.util.List;

import mekanism.common.Mekanism;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

public class ItemIngot extends ItemMekanism
{
	public IIcon[] icons = new IIcon[256];

	public static String[] en_USNames = {"Obsidian", "Osmium", "Bronze", "Glowstone", "Steel", "Copper", "Tin"};

	public ItemIngot(int id)
	{
		super(id);
		setHasSubtypes(true);
		setCreativeTab(Mekanism.tabMekanism);
	}

	@Override
	public void registerIcons(IIconRegister register)
	{
		for(int i = 0; i <= 6; i++)
		{
			icons[i] = register.registerIcon("mekanism:" + en_USNames[i] + "Ingot");
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
		for(int counter = 0; counter <= 6; ++counter)
		{
			itemList.add(new ItemStack(this, 1, counter));
		}
	}

	@Override
	public String getUnlocalizedName(ItemStack item)
	{
		return "item." + en_USNames[item.getItemDamage()].toLowerCase() + "Ingot";
	}
}
