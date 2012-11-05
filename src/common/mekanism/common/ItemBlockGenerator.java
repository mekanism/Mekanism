package mekanism.common;

import net.minecraft.src.*;

/**
 * Item class for handling multiple generator block IDs.
 * 0: Heat Generator
 * @author AidanBrady
 *
 */
public class ItemBlockGenerator extends ItemBlock
{
	public Block metaBlock;
	
	public ItemBlockGenerator(int id, Block block)
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
				name = "HeatGenerator";
				break;
			default:
				name = "Unknown";
				break;
		}
		return getItemName() + "." + name;
	}
}
