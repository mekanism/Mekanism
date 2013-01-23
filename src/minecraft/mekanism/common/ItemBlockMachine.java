package mekanism.common;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

/**
 * Item class for handling multiple machine block IDs.
 * 0: Enrichment Chamber
 * 1: Platinum Compressor
 * 2: Combiner
 * 3: Crusher
 * 4: Theoretical Elementizer
 * 5: Basic Smelting Factory
 * 6: Advanced Smelting Factory
 * 7: Elite Smelting Factory
 * 8: Metallurgic Infuser
 * 9: Purification Chamber
 * @author AidanBrady
 *
 */
public class ItemBlockMachine extends ItemBlock
{
	public Block metaBlock;
	
	public ItemBlockMachine(int id, Block block)
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
				name = "EnrichmentChamber";
				break;
			case 1:
				name = "PlatinumCompressor";
				break;
			case 2:
				name = "Combiner";
				break;
			case 3:
				name = "Crusher";
				break;
			case 4:
				name = "TheoreticalElementizer";
				break;
			case 5:
				name = "BasicSmeltingFactory";
				break;
			case 6:
				name = "AdvancedSmeltingFactory";
				break;
			case 7:
				name = "EliteSmeltingFactory";
				break;
			case 8:
				name = "MetallurgicInfuser";
				break;
			case 9:
				name = "PurificationChamber";
				break;
			default:
				name = "Unknown";
				break;
		}
		return getItemName() + "." + name;
	}
}
