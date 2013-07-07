package mekanism.common;

import java.util.List;

import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;

public class ItemIngot extends ItemMekanism
{
	public Icon[] icons = new Icon[256];
	public static String[] en_USNames = {"Obsidian", "Osmium", "Bronze", "Glowstone", "Steel"};
	
	public ItemIngot(int id)
	{
		super(id);
		setHasSubtypes(true);
		setCreativeTab(Mekanism.tabMekanism);
	}
	
	@Override
	public void registerIcons(IconRegister register)
	{
		for(int i = 0; i <= 4; i++)
		{
			icons[i] = register.registerIcon("mekanism:" + en_USNames[i] + "Ingot");
		}
	}

	@Override
	public Icon getIconFromDamage(int meta)
	{
		return icons[meta];
	}

	@Override
	public void getSubItems(int id, CreativeTabs tabs, List itemList)
	{
		for (int counter = 0; counter <= 4; ++counter)
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
