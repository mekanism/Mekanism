package mekanism.common.item;

import java.util.List;

import mekanism.common.base.IMetaItem;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ItemOtherDust extends ItemMekanism implements IMetaItem
{
	public static String[] subtypes = {"Diamond", "Steel", "null", "Sulfur",
									   "Lithium", "RefinedObsidian", "Obsidian"};
	
	public ItemOtherDust()
	{
		super();
		setHasSubtypes(true);
	}
	
	@Override
	public String getTexture(int meta)
	{
		if(meta > 1)
		{
			meta++;
		}
		
		return subtypes[meta] + "Dust";
	}
	
	@Override
	public int getVariants()
	{
		return subtypes.length-1;
	}

	@Override
	public void getSubItems(Item item, CreativeTabs tabs, List<ItemStack> itemList)
	{
		for(int counter = 0; counter < subtypes.length; counter++)
		{
			if(counter != 2)
			{
				itemList.add(new ItemStack(this, 1, counter));
			}
		}
	}

	@Override
	public String getUnlocalizedName(ItemStack item)
	{
		return "item." + subtypes[item.getItemDamage()].toLowerCase() + "Dust";
	}
}
