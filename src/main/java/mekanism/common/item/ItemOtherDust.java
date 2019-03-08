package mekanism.common.item;

import mekanism.common.base.IMetaItem;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import javax.annotation.Nonnull;
import java.util.Locale;

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
		if(meta == 2)
		{
			return null;
		}
		
		return subtypes[meta] + "Dust";
	}
	
	@Override
	public int getVariants()
	{
		return subtypes.length;
	}

	@Override
	public void getSubItems(@Nonnull CreativeTabs tabs, @Nonnull NonNullList<ItemStack> itemList)
	{
		if(!isInCreativeTab(tabs)) return;
		for(int counter = 0; counter < subtypes.length; counter++)
		{
			if(counter != 2)
			{
				itemList.add(new ItemStack(this, 1, counter));
			}
		}
	}

	@Nonnull
	@Override
	public String getTranslationKey(ItemStack item)
	{
		return "item." + subtypes[item.getItemDamage()].toLowerCase(Locale.ROOT) + "Dust";
	}
}
