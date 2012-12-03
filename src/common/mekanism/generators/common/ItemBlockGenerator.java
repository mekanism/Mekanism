package mekanism.generators.common;

import mekanism.generators.common.BlockGenerator.GeneratorType;
import net.minecraft.src.*;

/**
 * Item class for handling multiple generator block IDs.
 * 0: Heat Generator
 * 1: Solar Generator
 * 2: Electrolytic Separator
 * 3: Hydrogen Generator
 * 4: Bio-Generator
 * 5: Advanced Solar Generator
 * 6: Hydro Generator
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
    public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int metadata)
    {
		if(stack.getItemDamage() == GeneratorType.ADVANCED_SOLAR_GENERATOR.meta)
		{
	        boolean canPlace = true;
	        
	        if(world.getBlockId(x, y, z) != Block.tallGrass.blockID && world.getBlockId(x, y, z) != 0) 
	        	canPlace = false;
	        
	        if(world.getBlockId(x, y, z) != 0)
	        {
	        	if(Block.blocksList[world.getBlockId(x, y, z)].isBlockReplaceable(world, x, y, z)) 
	        		canPlace = true; 
	        }
	        
			for(int xPos=-1;xPos<=1;xPos++)
			{
				for(int zPos=-1;zPos<=1;zPos++)
				{
					if(world.getBlockId(x+xPos, y+2, z+zPos) != 0) 
						canPlace = false;
				}
			}
			
			if(canPlace)
			{
				return super.placeBlockAt(stack, player, world, x, y, z, side, hitX, hitY, hitZ, metadata);
			}
			return false;
		}
		
		return super.placeBlockAt(stack, player, world, x, y, z, side, hitX, hitY, hitZ, metadata);
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
			case 6:
				name = "HydroGenerator";
				break;
			default:
				name = "Unknown";
				break;
		}
		return getItemName() + "." + name;
	}
}
