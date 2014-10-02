package mekanism.api.util;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ItemInfo
{
	public Item item;
	public int meta;

	public ItemInfo(Item i, int j)
	{
		item = i;
		meta = j;
	}

	public static ItemInfo get(ItemStack stack)
	{
		return new ItemInfo(stack.getItem(), stack.getItemDamage());
	}

	@Override
	public boolean equals(Object obj)
	{
		return obj instanceof ItemInfo &&
				((ItemInfo)obj).item == item &&
				((ItemInfo)obj).meta == meta;
	}

	@Override
	public int hashCode()
	{
		int code = 1;
		code = 31 * code + System.identityHashCode(item);
		code = 7 * code + meta;
		return code;
	}
}
