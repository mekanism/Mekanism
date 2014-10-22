package mekanism.common.item;

import java.util.List;

import mekanism.common.Mekanism;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

public class ItemDirtyDust extends ItemMekanism
{
	public static final int OBSIDIAN = 0;
	public static final int LEAD = 1;
	
	private static final String[] values = {"Obsidian", "Lead"};
	
	public IIcon[] icons = new IIcon[256];

	public ItemDirtyDust()
	{
		super();
		setHasSubtypes(true);
		setCreativeTab(Mekanism.tabMekanism);
	}

	@Override
	public void registerIcons(IIconRegister register)
	{
		for(int i = 0; i < values.length; i++)
		{
			icons[i] = register.registerIcon("mekanism:Dirty" + values[i] + "Dust");
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
		for(int i = 0; i < values.length; i++)
		{
			itemList.add(new ItemStack(item, 1, i));
		}
	}

	@Override
	public String getUnlocalizedName(ItemStack item)
	{
		return "item.dirty" + values[item.getItemDamage()] + "Dust";
	}
}
