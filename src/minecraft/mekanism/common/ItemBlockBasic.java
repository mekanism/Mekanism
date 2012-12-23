package mekanism.common;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

/**
 * Item class for handling multiple metal block IDs.
 * 0: Platinum Block
 * 1: Redstone Block
 * 2: Refined Obsidian
 * 3: Coal Block
 * 4: Refined Glowstone
 * 5: Steel Block
 * 6: Control Panel
 * @author AidanBrady
 *
 */
public class ItemBlockBasic extends ItemBlock
{
	public Block metaBlock;
	
	public ItemBlockBasic(int id, Block block)
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
			case 5:
				name = "SteelBlock";
				break;
			case 6:
				name = "ControlPanel";
				break;
			default:
				name = "Unknown";
				break;
		}
		return getItemName() + "." + name;
	}
}
