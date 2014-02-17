package mekanism.api;

import net.minecraft.item.ItemStack;

public class ItemInfo
{	
	public int id;
	public int meta;
	
	public ItemInfo(int i, int j)
	{
		id = i;
		meta = j;
	}
	
	public static ItemInfo get(ItemStack stack)
	{
		return new ItemInfo(stack.itemID, stack.getItemDamage());
	}
	
	@Override
	public boolean equals(Object obj)
	{
		return obj instanceof ItemInfo && 
				((ItemInfo)obj).id == id && 
				((ItemInfo)obj).meta == meta;
	}
	
	@Override
	public int hashCode() 
	{
		int code = 1;
		code = 31 * code + id;
		code = 31 * code + meta;
		return code;
	}
}