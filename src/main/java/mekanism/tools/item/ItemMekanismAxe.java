package mekanism.tools.item;

import java.util.Set;

import mekanism.tools.common.MekanismTools;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

import com.google.common.collect.Sets;

public class ItemMekanismAxe extends ItemMekanismTool
{
	private static final Set<Block> EFFECTIVE_ON = Sets.newHashSet(new Block[] {Blocks.PLANKS, Blocks.BOOKSHELF, Blocks.LOG, Blocks.LOG2, Blocks.CHEST, Blocks.PUMPKIN, Blocks.LIT_PUMPKIN, Blocks.MELON_BLOCK, Blocks.LADDER, Blocks.WOODEN_BUTTON, Blocks.WOODEN_PRESSURE_PLATE});

	public ItemMekanismAxe(ToolMaterial tool)
	{
		super(MekanismTools.AXE_DAMAGE.get(tool), MekanismTools.AXE_SPEED.get(tool), tool, EFFECTIVE_ON);
	}

	@Override
	public float getStrVsBlock(ItemStack itemstack, IBlockState blockState)
	{
		if(blockState != null && blockState.getBlock() != null && blockState.getMaterial() == Material.WOOD)
		{
			return efficiencyOnProperMaterial;
		}
		else {
			return super.getStrVsBlock(itemstack, blockState);
		}
	}
}
