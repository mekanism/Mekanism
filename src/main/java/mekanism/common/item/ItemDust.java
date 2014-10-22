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
	//The indices for the types of dust defined here.
	public static final int R_OBSIDIAN = 0;
	public static final int DIAMOND = 1;
	public static final int LEAD = 2;
	public static final int SULFUR = 3;

	public IIcon[] icons = new IIcon[256];

	public static String[] en_USNames = { "Obsidian", "Diamond", "Lead", "Sulfur" };

	public ItemDust()
	{
		super();
		setHasSubtypes(true);
		setCreativeTab(Mekanism.tabMekanism);
	}

	@Override
	public void registerIcons(IIconRegister register)
	{
		for(int i = 0; i < en_USNames.length; i++)
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
		for(int counter = 0; counter < en_USNames.length; ++counter)
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
