package mekanism.tools.item;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

public class ItemMekanismPaxel extends ItemMekanismTool
{
	public ItemMekanismPaxel(ToolMaterial toolMaterial)
	{
		super(3, toolMaterial, new Block[0]);
	}

	@Override
	public float getDigSpeed(ItemStack stack, Block block, int meta)
	{
		return block != Blocks.bedrock ? efficiencyOnProperMaterial : 1.0F;
	}

	@Override
	public boolean canHarvestBlock(Block block, ItemStack stack)
	{
		if(block == Blocks.obsidian)
		{
			return toolMaterial.getHarvestLevel() == 3;
		}

		if(block == Blocks.diamond_block || block == Blocks.diamond_ore)
		{
			return toolMaterial.getHarvestLevel() >= 2;
		}

		if(block == Blocks.gold_block || block == Blocks.gold_ore)
		{
			return toolMaterial.getHarvestLevel() >= 2;
		}

		if(block == Blocks.iron_block || block == Blocks.iron_ore)
		{
			return toolMaterial.getHarvestLevel() >= 1;
		}

		if(block == Blocks.lapis_block || block == Blocks.lapis_ore)
		{
			return toolMaterial.getHarvestLevel() >= 1;
		}

		if(block == Blocks.redstone_ore || block == Blocks.lit_redstone_ore)
		{
			return toolMaterial.getHarvestLevel() >= 2;
		}

		if(block == Blocks.anvil)
		{
			return toolMaterial.getHarvestLevel() >= 0;
		}

		if(block == Blocks.snow || block == Blocks.snow_layer)
		{
			return true;
		}

		if(block.getMaterial() == Material.rock)
		{
			return true;
		}

		return block.getMaterial() == Material.iron;
	}
}
