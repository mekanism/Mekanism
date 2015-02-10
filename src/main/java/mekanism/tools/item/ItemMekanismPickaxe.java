package mekanism.tools.item;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

public class ItemMekanismPickaxe extends ItemMekanismTool
{
	private static Block blocksEffectiveAgainst[];

	public ItemMekanismPickaxe(ToolMaterial toolMaterial)
	{
		super(2, toolMaterial, blocksEffectiveAgainst);
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

		if(block.getMaterial() == Material.rock)
		{
			return true;
		}

		return block.getMaterial() == Material.iron;
	}

	@Override
	public float getDigSpeed(ItemStack itemstack, Block block, int meta)
	{
		if(block != null && (block.getMaterial() == Material.iron || block.getMaterial() == Material.anvil || block.getMaterial() == Material.rock))
		{
			return efficiencyOnProperMaterial;
		}
		else {
			return super.getDigSpeed(itemstack, block, meta);
		}
	}

	static
	{
		blocksEffectiveAgainst = (new Block[]
				{
					Blocks.cobblestone, Blocks.stone_slab, Blocks.double_stone_slab, Blocks.stone, Blocks.sandstone, Blocks.mossy_cobblestone, Blocks.iron_ore, Blocks.iron_block, Blocks.coal_ore, Blocks.gold_block,
					Blocks.gold_ore, Blocks.diamond_ore, Blocks.diamond_block, Blocks.ice, Blocks.netherrack, Blocks.lapis_ore, Blocks.lapis_block, Blocks.redstone_ore, Blocks.lit_redstone_ore, Blocks.rail,
					Blocks.detector_rail, Blocks.golden_rail, Blocks.activator_rail
				});
	}
}
