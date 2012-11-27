package mekanism.generators.common;

import net.minecraft.src.*;

/**
 * Item class for handling multiple generator block IDs.
 * 0: Heat Generator
 * 1: Solar Generator
 * 2: Electrolytic Separator
 * 3: Hydrogen Generator
 * 4: Bio-Generator
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
				name = "HeatGenerator";
				break;
			case 1:
				name = "SolarGenerator";
				break;
			case 2:
				name = "ElectrolyticSeparator";
				break;
			case 3:
				name = "HydrogenGenerator";
				break;
			case 4:
				name = "BioGenerator";
				break;
			case 5:
				name = "AdvancedSolarGenerator";
				break;
			default:
				name = "Unknown";
				break;
		}
		return getItemName() + "." + name;
	}
}
