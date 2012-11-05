package mekanism.common;

import net.minecraft.src.*;

/**
 * Item class for handling multiple metal block IDs.
 * 0: Platinum Block
 * 1: Redstone Block
 * 2: Refined Obsidian
 * 3: Coal Block
 * 4: Refined Glowstone
 * @author AidanBrady
 *
 */
public class ItemBlockMulti extends ItemBlock
{
	public Block metaBlock;
	
	public ItemBlockMulti(int id, Block block)
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
				name = "PlatinumBlock";
				break;
			case 1:
				name = "RedstoneBlock";
				break;
			case 2:
				name = "RefinedObsidian";
				break;
			case 3:
				name = "CoalBlock";
				break;
			case 4:
				name = "RefinedGlowstone";
				break;
			default:
				name = "Unknown";
				break;
		}
		return getItemName() + "." + name;
	}
}
