package net.uberkat.obsidian.common;

import net.minecraft.src.*;

/**
 * Item class for handling multiple IDs. 
 * 0: Platinum Ore
 * 1: Platinum Block
 * 2: Redstone Block
 * 3: Refined Obsidian
 * 4: Coal Block
 * 5: Refined Glowstone
 * @author AidanBrady
 *
 */
public class ItemMulti extends ItemBlock
{
	public Block metaBlock;
	
	public ItemMulti(int id, Block block)
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
			case 1:
				name = "PlatinumBlock";
				break;
			case 2:
				name = "RedstoneBlock";
				break;
			case 3:
				name = "RefinedObsidian";
				break;
			case 4:
				name = "CoalBlock";
				break;
			case 5:
				name = "RefinedGlowstone";
				break;
			default:
				name = "Unknown";
				break;
		}
		return getItemName() + "." + name;
	}
}
