package mekanism.common.item;

import java.util.List;

import mekanism.common.Mekanism;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

public class ItemDust extends ItemMekanism
{
	public IIcon[] icons = new IIcon[256];

	public static String[] en_USNames = {"Iron", "Gold", "Osmium",
										"Obsidian", "Diamond", "Steel",
										"Copper", "Tin", "Silver",
										"Lead", "Sulfur"};

	public ItemDust()
	{
		super();
		setHasSubtypes(true);
		setCreativeTab(Mekanism.tabMekanism);
	}

	@Override
	public void registerIcons(IIconRegister register)
	{
		for(int i = 0; i <= 10; i++)
		{
			icons[i] = register.registerIcon("mekanism:" + en_USNames[i] + "Dust");
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
		for(int counter = 0; counter <= 10; ++counter)
		{
			itemList.add(new ItemStack(item, 1, counter));
		}
	}

	@Override
	public String getUnlocalizedName(ItemStack item)
	{
		return "item." + en_USNames[item.getItemDamage()].toLowerCase() + "Dust";
	}
}
