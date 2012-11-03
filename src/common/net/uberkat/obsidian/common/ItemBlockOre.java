package net.uberkat.obsidian.common;

import net.minecraft.src.*;

/**
 * Item class for handling multiple ore block IDs.
 * 0: Platinum Ore
 * @author AidanBrady
 *
 */
public class ItemBlockOre extends ItemBlock
{
	public Block metaBlock;
	
	public ItemBlockOre(int id, Block block)
	{
		super(id);
		metaBlock = block;
		setHasSubtypes(true);
	}
	
	public int getMetadata(int i)
	{
		return i;
	}
	
	public int getIconFromDamage(int i)
	{
		return metaBlock.getBlockTextureFromSideAndMetadata(2, i);
	}
	
	public String getItemNameIS(ItemStack itemstack)
	{
		String name = "";
		switch(itemstack.getItemDamage())
		{
			case 0:
				name = "PlatinumOre";
				break;
			default:
				name = "Unknown";
				break;
		}
		return getItemName() + "." + name;
	}
}
