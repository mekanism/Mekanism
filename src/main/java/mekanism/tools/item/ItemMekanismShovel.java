package mekanism.tools.item;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

public class ItemMekanismShovel extends ItemMekanismTool
{
	private static Block blocksEffectiveAgainst[];

	public ItemMekanismShovel(ToolMaterial enumtoolmaterial)
	{
		super(1, enumtoolmaterial, blocksEffectiveAgainst);
	}

	@Override
	public boolean canHarvestBlock(Block block, ItemStack stack)
	{
		if(block == Blocks.snow_layer)
		{
			return true;
		}

		return block == Blocks.snow;
	}

	static
	{
		blocksEffectiveAgainst = (new Block[]
				{
					Blocks.grass, Blocks.dirt, Blocks.sand, Blocks.gravel, Blocks.snow_layer, Blocks.snow, Blocks.clay, Blocks.farmland, Blocks.soul_sand, Blocks.mycelium
				});
	}
}
