package mekanism.common.item;

import java.util.List;

import mekanism.common.Mekanism;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

public class ItemHDPE extends ItemMekanism
{
	public IIcon[] icons = new IIcon[256];

	public ItemHDPE()
	{
		super();
		setHasSubtypes(true);
		setCreativeTab(Mekanism.tabMekanism);
	}

	@Override
	public void registerIcons(IIconRegister register)
	{
		for(int i = 0; i < PlasticItem.values().length; i++)
		{
			icons[i] = register.registerIcon("mekanism:" + PlasticItem.values()[i].getName());
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
		for(int counter = 0; counter < PlasticItem.values().length; counter++)
		{
			itemList.add(new ItemStack(item, 1, counter));
		}
	}

	@Override
	public String getUnlocalizedName(ItemStack item)
	{
		return "item." + PlasticItem.values()[item.getItemDamage()].getName();
	}

	public enum PlasticItem
	{
		PELLET("HDPEPellet"),
		ROD("HDPERod"),
		SHEET("HDPESheet"),
		STICK("PlaStick");

		private String name;

		private PlasticItem(String itemName)
		{
			name = itemName;
		}

		public String getName()
		{
			return name;
		}
	}
}
