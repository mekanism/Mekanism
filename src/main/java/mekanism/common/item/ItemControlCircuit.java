package mekanism.common.item;

import java.util.List;

import mekanism.common.Mekanism;
import mekanism.common.Tier.BaseTier;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

public class ItemControlCircuit extends ItemMekanism
{
	public IIcon[] icons = new IIcon[256];

	public ItemControlCircuit()
	{
		super();
		setHasSubtypes(true);
		setCreativeTab(Mekanism.tabMekanism);
	}

	@Override
	public void registerIcons(IIconRegister register)
	{
		for(BaseTier tier : BaseTier.values())
		{
			if(tier.isObtainable())
			{
				icons[tier.ordinal()] = register.registerIcon("mekanism:" + tier.getName() + "ControlCircuit");
			}
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
		for(BaseTier tier : BaseTier.values())
		{
			if(tier.isObtainable())
			{
				itemList.add(new ItemStack(item, 1, tier.ordinal()));
			}
		}
	}

	@Override
	public String getUnlocalizedName(ItemStack item)
	{
		return "item." + BaseTier.values()[item.getItemDamage()].getName() + "ControlCircuit";
	}
}
