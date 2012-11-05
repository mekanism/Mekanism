package mekanism.common;

import net.minecraft.src.*;

/**
 * Item class for handling multiple power unit block IDs.
 * 0: Power Unit
 * 1: Advanced Power Unit
 * @author AidanBrady
 *
 */
public class ItemBlockPowerUnit extends ItemBlock
{
	public Block metaBlock;
	
	public ItemBlockPowerUnit(int id, Block block)
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
				name = "PowerUnit";
				break;
			case 1:
				name = "AdvancedPowerUnit";
				break;
			default:
				name = "Unknown";
				break;
		}
		return getItemName() + "." + name;
	}
}
