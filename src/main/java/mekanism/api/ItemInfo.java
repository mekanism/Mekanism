package mekanism.api;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

public class ItemInfo
{
	public Block block;
	public int meta;

	public ItemInfo(Block b, int j)
	{
		block = b;
		meta = j;
	}

	public static ItemInfo get(ItemStack stack)
	{
		return new ItemInfo(Block.getBlockFromItem(stack.getItem()), stack.getItemDamage());
	}

	@Override
	public boolean equals(Object obj)
	{
		return obj instanceof ItemInfo &&
				((ItemInfo)obj).block == block &&
				((ItemInfo)obj).meta == meta;
	}

	@Override
	public int hashCode()
	{
		int code = 1;
		code = 31 * code + block.getUnlocalizedName().hashCode();
		code = 31 * code + meta;
		return code;
	}
}