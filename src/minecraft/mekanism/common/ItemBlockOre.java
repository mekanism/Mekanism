package mekanism.common;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

/**
 * Item class for handling multiple ore block IDs.
 * 0: Osmium Ore
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
	
	@Override
	public int getMetadata(int i)
	{
		return i;
	}
	
	@Override
	public int getIconFromDamage(int i)
	{
		return metaBlock.getBlockTextureFromSideAndMetadata(2, i);
	}
	
	@Override
	public String getItemNameIS(ItemStack itemstack)
	{
		String name = "";
		switch(itemstack.getItemDamage())
		{
			case 0:
				name = "OsmiumOre";
				break;
			default:
				name = "Unknown";
				break;
		}
		return getItemName() + "." + name;
	}
}
