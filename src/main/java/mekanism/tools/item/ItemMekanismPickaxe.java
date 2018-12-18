package mekanism.tools.item;

import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

import com.google.common.collect.Sets;

public class ItemMekanismPickaxe extends ItemMekanismTool
{
    private static final Set<Block> EFFECTIVE_ON = Sets.newHashSet(Blocks.ACTIVATOR_RAIL, Blocks.COAL_ORE, Blocks.COBBLESTONE, Blocks.DETECTOR_RAIL, Blocks.DIAMOND_BLOCK, Blocks.DIAMOND_ORE, Blocks.DOUBLE_STONE_SLAB, Blocks.GOLDEN_RAIL, Blocks.GOLD_BLOCK, Blocks.GOLD_ORE, Blocks.ICE, Blocks.IRON_BLOCK, Blocks.IRON_ORE, Blocks.LAPIS_BLOCK, Blocks.LAPIS_ORE, Blocks.LIT_REDSTONE_ORE, Blocks.MOSSY_COBBLESTONE, Blocks.NETHERRACK, Blocks.PACKED_ICE, Blocks.RAIL, Blocks.REDSTONE_ORE, Blocks.SANDSTONE, Blocks.RED_SANDSTONE, Blocks.STONE, Blocks.STONE_SLAB, Blocks.STONE_BUTTON, Blocks.STONE_PRESSURE_PLATE);

	public ItemMekanismPickaxe(ToolMaterial toolMaterial)
	{
		super(1, -2.8F, toolMaterial, EFFECTIVE_ON);
	}

	@Override
	public boolean canHarvestBlock(IBlockState state, ItemStack stack)
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

		if(state.getMaterial() == Material.ROCK)
		{
			return true;
		}

		return state.getMaterial() == Material.IRON;
	}

	@Override
	public float getStrVsBlock(ItemStack itemstack, IBlockState blockState)
	{
		if(blockState != null && blockState.getBlock() != null && (blockState.getMaterial() == Material.IRON || blockState.getMaterial() == Material.ANVIL || blockState.getMaterial() == Material.ROCK))
		{
			return efficiencyOnProperMaterial;
		}
		else {
			return super.getStrVsBlock(itemstack, blockState);
		}
	}
}
