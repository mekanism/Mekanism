package mekanism.common.item;

import java.util.List;

import mekanism.common.Tier.BaseTier;
import mekanism.common.base.IMetaItem;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ItemControlCircuit extends ItemMekanism implements IMetaItem
{
	public ItemControlCircuit()
	{
		super();
		setHasSubtypes(true);
	}

	@Override
	public String getTexture(int meta)
	{
		return BaseTier.values()[meta].getSimpleName() + "ControlCircuit";
	}
	
	@Override
	public int getVariants()
	{
		return BaseTier.values().length-1;
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
		return "item." + BaseTier.values()[item.getItemDamage()].getSimpleName() + "ControlCircuit";
	}
}
