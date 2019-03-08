package mekanism.tools.item;

import java.util.HashSet;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class ItemMekanismPaxel extends ItemMekanismTool
{
	public ItemMekanismPaxel(ToolMaterial toolMaterial)
	{
		super(4, -2.4F, toolMaterial, new HashSet<>());
	}

	@Override
	public float getDestroySpeed(@Nonnull ItemStack stack, IBlockState blockState)
	{
		return blockState.getBlock() != Blocks.BEDROCK ? efficiency : 1.0F;
	}

	@Override
	public boolean canHarvestBlock(@Nonnull IBlockState state, ItemStack stack)
	{
		Block block = state.getBlock();
		
		if(block == Blocks.OBSIDIAN)
		{
			return toolMaterial.getHarvestLevel() == 3;
		}

		if(block == Blocks.DIAMOND_BLOCK || block == Blocks.DIAMOND_ORE)
		{
			return toolMaterial.getHarvestLevel() >= 2;
		}

		if(block == Blocks.GOLD_BLOCK || block == Blocks.GOLD_ORE)
		{
			return toolMaterial.getHarvestLevel() >= 2;
		}

		if(block == Blocks.IRON_BLOCK || block == Blocks.IRON_ORE)
		{
			return toolMaterial.getHarvestLevel() >= 1;
		}

		if(block == Blocks.LAPIS_BLOCK || block == Blocks.LAPIS_ORE)
		{
			return toolMaterial.getHarvestLevel() >= 1;
		}

		if(block == Blocks.REDSTONE_ORE || block == Blocks.LIT_REDSTONE_ORE)
		{
			return toolMaterial.getHarvestLevel() >= 2;
		}

		if(block == Blocks.ANVIL)
		{
			return toolMaterial.getHarvestLevel() >= 0;
		}

		if(block == Blocks.SNOW || block == Blocks.SNOW_LAYER)
		{
			return true;
		}

		if(state.getMaterial() == Material.ROCK)
		{
			return true;
		}

		return state.getMaterial() == Material.IRON;
	}
}
