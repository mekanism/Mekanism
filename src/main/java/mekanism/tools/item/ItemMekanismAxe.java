package mekanism.tools.item;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

public class ItemMekanismAxe extends ItemMekanismTool
{
	private static Block blocksEffectiveAgainst[];

	public ItemMekanismAxe(ToolMaterial enumtoolmaterial)
	{
		super(3, enumtoolmaterial, blocksEffectiveAgainst);
	}

	@Override
	public float getDigSpeed(ItemStack itemstack, IBlockState blockState)
	{
		if(blockState != null && blockState.getBlock() != null && blockState.getBlock().getMaterial() == Material.wood)
		{
			return efficiencyOnProperMaterial;
		}
		else {
			return super.getDigSpeed(itemstack, blockState);
		}
	}

	static
	{
		blocksEffectiveAgainst = (new Block[]
				{
					Blocks.planks, Blocks.bookshelf, Blocks.log, Blocks.log2, Blocks.chest, Blocks.wooden_slab, Blocks.double_wooden_slab, Blocks.pumpkin, Blocks.lit_pumpkin
				});
	}
}
